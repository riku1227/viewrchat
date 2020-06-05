package com.riku1227.viewrchat.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.net.UriCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.riku1227.viewrchat.R

class UserLinksRecyclerAdapter(private val context: Context, private val linksList: List<String>, private val activity: Activity) : RecyclerView.Adapter<UserLinksRecyclerAdapter.UserLinksRecyclerViewHolder>() {
    class UserLinksRecyclerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recyclerUserLinksButton: MaterialButton = view.findViewById(R.id.recyclerUserLinksButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserLinksRecyclerViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_user_links, parent, false)
        return UserLinksRecyclerViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return linksList.size
    }

    override fun onBindViewHolder(holder: UserLinksRecyclerViewHolder, position: Int) {
        val link = linksList[position]

        holder.recyclerUserLinksButton.let {
            it.text = link

            it.setOnClickListener {
                val uri = Uri.parse(link)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                activity.startActivity(intent)
            }

            if(link.indexOf("twitter.com") != -1) {
                val twitterColorStateList = ContextCompat.getColorStateList(context, R.color.twitter_color)
                it.setTextColor(twitterColorStateList)
                it.rippleColor = twitterColorStateList
                it.strokeColor = twitterColorStateList
            } else if (link.indexOf("booth.pm") != -1) {
                val boothColorStateList = ContextCompat.getColorStateList(context, R.color.booth_color)
                it.setTextColor(boothColorStateList)
                it.rippleColor = boothColorStateList
                it.strokeColor = boothColorStateList
            } else if (link.indexOf("github.com") != -1) {
                val githubColorStateList = ContextCompat.getColorStateList(context, R.color.github_color)
                it.setTextColor(githubColorStateList)
                it.rippleColor = githubColorStateList
                it.strokeColor = githubColorStateList
            }
        }
    }
}