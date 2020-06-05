package com.riku1227.viewrchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.riku1227.viewrchat.R

class UserLanguagesRecyclerAdapter(private val context: Context, private val languagesList: List<String>) : RecyclerView.Adapter<UserLanguagesRecyclerAdapter.UserLanguagesRecyclerViewHolder>() {
    class UserLanguagesRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerUserLanguage: TextView = view.findViewById(R.id.recyclerUserLanguage)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserLanguagesRecyclerViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_user_languages, parent, false)
        return UserLanguagesRecyclerViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return languagesList.size
    }

    override fun onBindViewHolder(holder: UserLanguagesRecyclerViewHolder, position: Int) {
        holder.recyclerUserLanguage.text = languagesList[position]
    }
}