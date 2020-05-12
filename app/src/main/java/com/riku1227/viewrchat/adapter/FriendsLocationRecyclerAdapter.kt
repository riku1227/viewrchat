package com.riku1227.viewrchat.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.system.CacheSystem
import com.riku1227.viewrchat.system.ErrorHandling
import com.riku1227.vrchatlin.VRChatlin
import com.riku1227.vrchatlin.model.VRChatUser
import com.riku1227.vrchatlin.model.VRChatWorld
import com.riku1227.vrchatlin.model.VRChatWorldInstance
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

class FriendsLocationRecyclerAdapter(private val context: Context, private val fragment: Fragment, private val locationMap: MutableMap<String, ArrayList<VRChatUser>>, private val locationList: ArrayList<String>) : RecyclerView.Adapter<FriendsLocationRecyclerAdapter.FriendsLocationRecyclerViewHolder>() {

    class FriendsLocationRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recyclerFriendsLocationWorldImage: ImageView = view.findViewById(R.id.recyclerFriendsLocationWorldImage)
        val recyclerFriendsLocationWorldName: TextView = view.findViewById(R.id.recyclerFriendsLocationWorldName)
        val recyclerFriendsLocationInstanceType: TextView = view.findViewById(R.id.recyclerFriendsLocationInstanceType)
        val recyclerFriendsLocationInstanceNUsers: TextView = view.findViewById(R.id.recyclerFriendsLocationInstanceNUsers)
        val recyclerFriendsLocationInstanceNFriends: TextView = view.findViewById(R.id.recyclerFriendsLocationInstanceNFriends)
        val recyclerFriendsLocationWorldDescription: TextView = view.findViewById(R.id.recyclerFriendsLocationWorldDescription)
        val recyclerFriendsLocationFriendsList: RecyclerView = view.findViewById(R.id.recyclerFriendsLocationFriendsList)
    }

    private val locationWorldDataMap = mutableMapOf<String, VRChatWorld>()
    private val locationInstanceDataMap = mutableMapOf<String, VRChatWorldInstance>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsLocationRecyclerViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_friends_location_card, parent, false)
        return FriendsLocationRecyclerViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: FriendsLocationRecyclerViewHolder, position: Int) {
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

        if(locationWorldDataMap[worldId] == null) {
            val dispo01 = apiService.getWorldByID(worldId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        locationWorldDataMap[worldId] = it
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
        } else {
            val worldData = locationWorldDataMap[worldId]!!
            holder.recyclerFriendsLocationWorldName.text = worldData.name
            holder.recyclerFriendsLocationWorldDescription.text = worldData.description
            val disposable3 = CacheSystem.loadImage(context, CacheSystem.CacheType.WORLD_IMAGE, worldData.id, worldData.thumbnailImageUrl)
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
        }

        if(locationInstanceDataMap[locationList[position]] == null) {
            val dispo02 = apiService.getWorldInstanceByID(worldId, instanceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        locationInstanceDataMap[locationList[position]] = it
                        holder.recyclerFriendsLocationInstanceType.text = it.type.toUpperCase(Locale.getDefault())
                        holder.recyclerFriendsLocationInstanceNUsers.text = "${it.n_users} / ${it.capacity}"
                        locationMap[locationList[position]]?.let { arrayUser ->
                            holder.recyclerFriendsLocationInstanceNFriends.text = arrayUser.size.toString()
                        }
                    },
                    {
                        ErrorHandling.onNetworkError(it, context, fragment = fragment)
                    }
                )
        } else {
            val locationInstanceData = locationInstanceDataMap[locationList[position]]!!
            holder.recyclerFriendsLocationInstanceType.text = locationInstanceData.type.toUpperCase(Locale.getDefault())
            holder.recyclerFriendsLocationInstanceNUsers.text = "${locationInstanceData.n_users} / ${locationInstanceData.capacity}"
            locationMap[locationList[position]]?.let { arrayUser ->
                holder.recyclerFriendsLocationInstanceNFriends.text = arrayUser.size.toString()
            }
        }

        locationMap[locationList[position]]?.let { arrayUser ->
            val adapter = FriendsLocationFiendsRecyclerAdapter(context, arrayUser)
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            holder.recyclerFriendsLocationFriendsList.adapter = adapter
            holder.recyclerFriendsLocationFriendsList.layoutManager = layoutManager
        }
    }

}