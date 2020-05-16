package com.riku1227.viewrchat.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.system.CacheSystem
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
        val recyclerFriendsListFriendsStatus: TextView = view.findViewById(R.id.recyclerFriendsListFriendsStatus)
        val recyclerFriendsListStatusDescription: TextView = view.findViewById(R.id.recyclerFriendsListStatusDescription)
        val recyclerFriendsListBio: TextView = view.findViewById(R.id.recyclerFriendsListBio)
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
        holder.recyclerFriendsListLastPlatform.text = friend.last_platform
        holder.recyclerFriendsListFriendsStatus.text = friend.status

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