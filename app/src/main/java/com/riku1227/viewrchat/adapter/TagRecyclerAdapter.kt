package com.riku1227.viewrchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.riku1227.viewrchat.R

class TagRecyclerAdapter(private val context: Context, private val tagList: List<String>) : RecyclerView.Adapter<TagRecyclerAdapter.TagRecyclerViewHolder>() {
    class TagRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerTagText: TextView = view.findViewById(R.id.recyclerTagText)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TagRecyclerViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.recycler_tag, parent, false)
        return TagRecyclerViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return tagList.size
    }

    override fun onBindViewHolder(holder: TagRecyclerViewHolder, position: Int) {
        holder.recyclerTagText.text = tagList[position]
    }
}