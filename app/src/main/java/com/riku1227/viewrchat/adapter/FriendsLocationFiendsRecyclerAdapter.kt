package com.riku1227.viewrchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.system.CacheSystem
import com.riku1227.vrchatlin.model.VRChatUser
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class FriendsLocationFiendsRecyclerAdapter(private val context: Context, private val userList: List<VRChatUser>) : RecyclerView.Adapter<FriendsLocationFiendsRecyclerAdapter.FriendsLocationFiendsViewHolder>() {
    class FriendsLocationFiendsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recyclerFriendsLocationFriendsImage: ImageView = view.findViewById(R.id.recyclerFriendsLocationFriendsImage)
        val recyclerFriendsLocationFriendsUserName: TextView = view.findViewById(R.id.recyclerFriendsLocationFriendsUserName)
        val recyclerFriendsLocationFriendsLastPlatform: TextView = view.findViewById(R.id.recyclerFriendsLocationFriendsLastPlatform)
        val recyclerFriendsLocationFriendsStatus: TextView = view.findViewById(R.id.recyclerFriendsLocationFriendsStatus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendsLocationFiendsViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_friends_location_friends_list, parent, false)
        return FriendsLocationFiendsViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: FriendsLocationFiendsViewHolder, position: Int) {
        userList[position].let {
            CacheSystem.loadImage(context, CacheSystem.CacheType.USER_AVATAR_IMAGE, it.id, it.currentAvatarThumbnailImageUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { imageFile ->
                        Picasso.get().load(imageFile)
                            .centerCrop()
                            .fit()
                            .into(holder.recyclerFriendsLocationFriendsImage)
                    },
                    {}
                )

            holder.recyclerFriendsLocationFriendsUserName.text = it.displayName
            holder.recyclerFriendsLocationFriendsLastPlatform.text = it.last_platform
            holder.recyclerFriendsLocationFriendsStatus.text = it.status!!.toUpperCase(Locale.getDefault())
        }
    }


}