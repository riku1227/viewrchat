package com.riku1227.viewrchat.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.system.CacheSystem
import com.riku1227.viewrchat.util.VRCUtil
import com.riku1227.viewrchat.util.setTextViewDrawableColor
import com.riku1227.vrchatlin.VRChatlin
import com.riku1227.vrchatlin.model.VRChatUser
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FriendsListRecyclerAdapter(private val context: Context, private val compositeDisposable: CompositeDisposable , var friendsList: List<VRChatUser>) : RecyclerView.Adapter<FriendsListRecyclerAdapter.FriendsListRecyclerViewHolder>() {
    private var isNowLoad = false
    private var isOffline = false
    private var currentCount = 0
    private var onlineCount = 0
    private var offlineCount = 0

    init {
        currentCount = itemCount
    }

    class FriendsListRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recyclerFriendsListUserAvatarImage: ImageView = view.findViewById(R.id.recyclerFriendsListUserAvatarImage)
        val recyclerFriendsListUserName: TextView = view.findViewById(R.id.recyclerFriendsListUserName)
        val recyclerFriendsListLastPlatform: TextView = view.findViewById(R.id.recyclerFriendsListLastPlatform)
        val recyclerFriendsListFriendsStatusIcon: ImageView = view.findViewById(R.id.recyclerFriendsListFriendsStatusIcon)
        val recyclerFriendsListFriendsStatus: TextView = view.findViewById(R.id.recyclerFriendsListFriendsStatus)
        val recyclerFriendsListStatusDescription: TextView = view.findViewById(R.id.recyclerFriendsListStatusDescription)
        val recyclerFriendsListBio: TextView = view.findViewById(R.id.recyclerFriendsListBio)
        val recyclerFriendsListLocationImage: ImageView = view.findViewById(R.id.recyclerFriendsListLocationImage)
        val recyclerFriendsListLocationTextRoot: LinearLayout = view.findViewById(R.id.recyclerFriendsListLocationTextRoot)
        val recyclerFriendsListLocationName: TextView = view.findViewById(R.id.recyclerFriendsListLocationName)
        val recyclerFriendsListLocationInstanceType: TextView = view.findViewById(R.id.recyclerFriendsListLocationInstanceType)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendsListRecyclerViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_friends_list_item, parent, false)
        return FriendsListRecyclerViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    override fun onBindViewHolder(holder: FriendsListRecyclerViewHolder, position: Int) {
        if(position > itemCount - 3 && !isNowLoad) {
            nextLoadList()
        }

        val friend = friendsList[position]

        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val drawable = BitmapDrawable(context.resources, bitmap)

        holder.recyclerFriendsListUserAvatarImage.setImageDrawable(drawable)
        holder.recyclerFriendsListUserAvatarImage.outlineProvider = ViewRChat.imageRadiusOutlineProvider


        val disposable = CacheSystem.loadImage(context, CacheSystem.CacheType.USER_AVATAR_IMAGE, friend.id, friend.currentAvatarThumbnailImageUrl, friend.status == "offline")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Picasso.get()
                        .load(it)
                        .centerCrop()
                        .fit()
                        .placeholder(drawable)
                        .into(holder.recyclerFriendsListUserAvatarImage)
                },
                {}
            )
        compositeDisposable.add(disposable)

        holder.recyclerFriendsListUserName.text = friend.displayName
        holder.recyclerFriendsListLastPlatform.text = context.resources.getString(R.string.general_last_login_platform, VRCUtil.getLastLoginPlatform(friend.last_platform))
        holder.recyclerFriendsListFriendsStatus.text = friend.status?.toUpperCase()
        holder.recyclerFriendsListFriendsStatusIcon.setColorFilter(VRCUtil.getStatusIconColor(context, friend.status))

        if(friend.statusDescription.isNullOrEmpty()) {
            holder.recyclerFriendsListStatusDescription.visibility = View.GONE
        } else {
            holder.recyclerFriendsListStatusDescription.visibility = View.VISIBLE
            holder.recyclerFriendsListStatusDescription.text = friend.statusDescription
        }

        if(friend.bio.isNullOrEmpty()) {
            holder.recyclerFriendsListBio.visibility = View.GONE
        } else {
            holder.recyclerFriendsListBio.visibility = View.VISIBLE
            holder.recyclerFriendsListBio.text = friend.bio
        }

        holder.recyclerFriendsListLocationImage.outlineProvider = ViewRChat.imageRadiusOutlineProvider
        holder.recyclerFriendsListLocationImage.setImageDrawable(drawable)

        friend.location?.let { friendLocation ->
            when (friendLocation) {
                "offline" -> {
                    holder.recyclerFriendsListLocationImage.visibility = View.GONE
                    holder.recyclerFriendsListLocationTextRoot.visibility = View.GONE
                }

                "private" -> {
                    holder.recyclerFriendsListLocationImage.visibility = View.VISIBLE
                    holder.recyclerFriendsListLocationTextRoot.visibility = View.VISIBLE
                    holder.recyclerFriendsListLocationInstanceType.visibility = View.GONE

                    holder.recyclerFriendsListLocationName.text = context.resources.getString(R.string.general_private_instance)

                    val loadVRChatDisposable = CacheSystem.loadImage(context, CacheSystem.CacheType.WORLD_IMAGE, "private_image", CacheSystem.VRC_ASSETS_PRIVATE_WORLD_IMAGE_URL)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { imgFile ->
                                Picasso.get()
                                    .load(imgFile)
                                    .placeholder(drawable)
                                    .centerCrop()
                                    .fit()
                                    .into(holder.recyclerFriendsListLocationImage)
                            },
                            {}
                        )
                    compositeDisposable.add(loadVRChatDisposable)
                }

                else -> {
                    holder.recyclerFriendsListLocationImage.visibility = View.VISIBLE
                    holder.recyclerFriendsListLocationTextRoot.visibility = View.VISIBLE
                    holder.recyclerFriendsListLocationInstanceType.visibility = View.VISIBLE

                    val splitLocation = friendLocation.split(":")
                    val worldId = splitLocation[0]
                    val instanceId = splitLocation[1]
                    val loadVRChatDisposable = CacheSystem.loadVRChatWorld(context, worldId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                holder.recyclerFriendsListLocationName.text = it.name
                                holder.recyclerFriendsListLocationInstanceType.text = VRCUtil.getInstanceTypeFromInstanceID(instanceId)

                                CacheSystem.loadImage(context, CacheSystem.CacheType.WORLD_IMAGE, it.id, it.thumbnailImageUrl)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        { imgFile ->
                                            Picasso.get()
                                                .load(imgFile)
                                                .placeholder(drawable)
                                                .centerCrop()
                                                .fit()
                                                .into(holder.recyclerFriendsListLocationImage)
                                        },
                                        {
                                        }
                                    )
                            },
                            {
                            }
                        )
                    compositeDisposable.add(loadVRChatDisposable)
                }
            }
        }
    }

    private fun nextLoadList() {
        var isExecute = false
        if(currentCount >= 50) {
            isExecute = true
        } else if (!isOffline) {
            isExecute = true
            isOffline = true
        }

        if(isExecute) {
            isNowLoad = true
            val offset = if(isOffline) {
                offlineCount
            } else {
                onlineCount
            }

            val disposable = VRChatlin.get(context).APIService().getFriends(offline = isOffline, n = 50, offset = offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        currentCount = it.size
                        if(isOffline) {
                            offlineCount += it.size
                        } else {
                            onlineCount += it.size
                        }

                        val currentCount = friendsList.size
                        friendsList = friendsList.plus(it)
                        this.notifyItemRangeChanged(currentCount, friendsList.size)
                        isNowLoad = false
                    },
                    {}
                )
            compositeDisposable.add(disposable)
        }
    }

    fun resetCount() {
        currentCount = itemCount
        onlineCount = 0
        offlineCount = 0
        isOffline = false
        isNowLoad = false
    }
}