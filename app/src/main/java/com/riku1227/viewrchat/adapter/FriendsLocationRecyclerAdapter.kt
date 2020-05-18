package com.riku1227.viewrchat.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.system.CacheSystem
import com.riku1227.viewrchat.system.ErrorHandling
import com.riku1227.viewrchat.util.VRCUtil
import com.riku1227.vrchatlin.VRChatlin
import com.riku1227.vrchatlin.model.VRChatUser
import com.riku1227.vrchatlin.model.VRChatWorldInstance
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

class FriendsLocationRecyclerAdapter(
    private val context: Context, private val fragment: Fragment, private val compositeDisposable: CompositeDisposable,
    var locationMap: MutableMap<String, ArrayList<VRChatUser>>,
    var locationList: ArrayList<String>, var currentCount: Int
) : RecyclerView.Adapter<FriendsLocationRecyclerAdapter.FriendsLocationRecyclerViewHolder>() {

    class FriendsLocationRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recyclerFriendsLocationWorldImage: ImageView = view.findViewById(R.id.recyclerFriendsLocationWorldImage)
        val recyclerFriendsLocationWorldName: TextView = view.findViewById(R.id.recyclerFriendsLocationWorldName)
        val recyclerFriendsLocationInstanceType: TextView = view.findViewById(R.id.recyclerFriendsLocationInstanceType)
        val recyclerFriendsLocationInstanceNUsers: TextView = view.findViewById(R.id.recyclerFriendsLocationInstanceNUsers)
        val recyclerFriendsLocationInstanceNFriends: TextView = view.findViewById(R.id.recyclerFriendsLocationInstanceNFriends)
        val recyclerFriendsLocationInviteMeButton: Button = view.findViewById(R.id.recyclerFriendsLocationInviteMeButton)
        val recyclerFriendsLocationUpdateInstance: Button = view.findViewById(R.id.recyclerFriendsLocationUpdateInstance)
        val recyclerFriendsLocationWorldDescription: TextView = view.findViewById(R.id.recyclerFriendsLocationWorldDescription)
        val recyclerFriendsLocationFriendsList: RecyclerView = view.findViewById(R.id.recyclerFriendsLocationFriendsList)
    }

    private val locationInstanceDataMap = mutableMapOf<String, VRChatWorldInstance>()

    private var isNowLoad = false
    private var totalCount = currentCount


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsLocationRecyclerViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_friends_location_card, parent, false)
        return FriendsLocationRecyclerViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: FriendsLocationRecyclerViewHolder, position: Int) {

        if(position > itemCount - 3 && !isNowLoad) {
            nextLoadList()
        }

        val splitLocation = locationList[position].split(":")
        val worldId = splitLocation[0]
        val instanceId = splitLocation[1]

        val apiService = VRChatlin.get(context).APIService()

        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val drawable = BitmapDrawable(context.resources, bitmap)
        holder.recyclerFriendsLocationWorldImage.setImageDrawable(drawable)

        holder.recyclerFriendsLocationWorldName.text = ""
        holder.recyclerFriendsLocationWorldDescription.text = ""
        holder.recyclerFriendsLocationInstanceType.text = ""
        holder.recyclerFriendsLocationInstanceNUsers.text = ""
        holder.recyclerFriendsLocationInstanceNFriends.text = ""

        holder.recyclerFriendsLocationWorldImage.outlineProvider = ViewRChat.imageRadiusOutlineProvider

        val loadVRChatDisposable = CacheSystem.loadVRChatWorld(context, worldId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    holder.recyclerFriendsLocationWorldName.text = it.name
                    holder.recyclerFriendsLocationWorldDescription.text = it.description
                    CacheSystem.loadImage(context, CacheSystem.CacheType.WORLD_IMAGE, it.id, it.thumbnailImageUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { imgFile ->
                                Picasso.get()
                                    .load(imgFile)
                                    .centerCrop()
                                    .fit()
                                    .into(holder.recyclerFriendsLocationWorldImage)
                            },
                            { error ->
                                Log.d("ViewRChat", error.toString())
                            }
                        )
                },
                {
                    ErrorHandling.onNetworkError(it, context, fragment = fragment)
                }
            )

        compositeDisposable.add(loadVRChatDisposable)

        if(locationInstanceDataMap[locationList[position]] == null) {
            val worldInstanceDisposable = apiService.getWorldInstanceByID(worldId, instanceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        locationInstanceDataMap[locationList[position]] = it
                        holder.recyclerFriendsLocationInstanceType.text = VRCUtil.getInstanceTypeFromInstanceID(it.id)
                        holder.recyclerFriendsLocationInstanceNUsers.text = context.resources.getString(R.string.fragment_friends_location_instance_user_count_text, it.n_users, it.capacity)
                        locationMap[locationList[position]]?.let { arrayUser ->
                            holder.recyclerFriendsLocationInstanceNFriends.text = arrayUser.size.toString()
                        }
                    },
                    {
                        ErrorHandling.onNetworkError(it, context, fragment = fragment)
                    }
                )
            compositeDisposable.add(worldInstanceDisposable)
        } else {
            val locationInstanceData = locationInstanceDataMap[locationList[position]]!!
            holder.recyclerFriendsLocationInstanceType.text = VRCUtil.getInstanceTypeFromInstanceID(locationInstanceData.id)
            holder.recyclerFriendsLocationInstanceNUsers.text = context.resources.getString(R.string.fragment_friends_location_instance_user_count_text,
                locationInstanceData.n_users, locationInstanceData.capacity)
            locationMap[locationList[position]]?.let { arrayUser ->
                holder.recyclerFriendsLocationInstanceNFriends.text = arrayUser.size.toString()
            }
        }

        holder.recyclerFriendsLocationInviteMeButton.setOnClickListener {
            VRChatlin.get(context).APIService().postInviteMe(worldId, instanceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Toast.makeText(context, context.resources.getString(R.string.fragment_friends_location_sent_invite), Toast.LENGTH_SHORT).show()
                    },
                    {}
                )
        }

        holder.recyclerFriendsLocationUpdateInstance.setOnClickListener {
            val worldInstanceDisposable = apiService.getWorldInstanceByID(worldId, instanceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        locationInstanceDataMap[locationList[position]] = it
                        holder.recyclerFriendsLocationInstanceType.text = VRCUtil.getInstanceTypeFromInstanceID(it.id)
                        holder.recyclerFriendsLocationInstanceNUsers.text = context.resources.getString(R.string.fragment_friends_location_instance_user_count_text, it.n_users, it.capacity)
                        locationMap[locationList[position]]?.let { arrayUser ->
                            holder.recyclerFriendsLocationInstanceNFriends.text = arrayUser.size.toString()
                        }
                        Toast.makeText(context, context.resources.getString(R.string.fragment_friends_location_updated_instance_info), Toast.LENGTH_SHORT).show()
                    },
                    {
                        ErrorHandling.onNetworkError(it, context, fragment = fragment)
                    }
                )
            compositeDisposable.add(worldInstanceDisposable)
        }

        locationMap[locationList[position]]?.let { arrayUser ->
            val adapter = FriendsLocationFiendsRecyclerAdapter(context, arrayUser)
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            holder.recyclerFriendsLocationFriendsList.adapter = adapter
            holder.recyclerFriendsLocationFriendsList.layoutManager = layoutManager
        }
    }

    private fun nextLoadList() {
        if(currentCount >= 50) {
            isNowLoad = true
            val getFriendsDisposable = VRChatlin.get(context).APIService().getFriends(offline = false, n = 50, offset = totalCount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        currentCount = it.size
                        totalCount += it.size

                        for (user in it) {
                            user.location?.let {  userLocation ->
                                if(userLocation != "private" && userLocation != "offline") {
                                    if(locationMap[userLocation] == null) {
                                        locationMap[userLocation] = arrayListOf(user)
                                        locationList.add(userLocation)
                                    } else {
                                        locationMap[userLocation]!!.add(user)
                                    }
                                }
                            }
                        }

                        this.notifyDataSetChanged()
                    },
                    {}
                )
            compositeDisposable.add(getFriendsDisposable)
        }
    }

}