package com.riku1227.viewrchat.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.activity.TutorialActivity

class TutorialRecyclerAdapter(private val context: Context, private val viewPager2: ViewPager2, private val activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemCount = 2

    class Page01ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val pagerTutorial01NextButton: Button = view.findViewById(R.id.pagerTutorial01NextButton)
    }

    class Page02ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val pagerTutorial02AgreeCheckBox: CheckBox = view.findViewById(R.id.pagerTutorial02AgreeCheckBox)
        val pagerTutorial02NextButton: Button = view.findViewById(R.id.pagerTutorial02NextButton)
    }

    class Page03ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val pagerTutorial03LoginButton: Button = view.findViewById(R.id.pagerTutorial03LoginButton)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            0 -> {
                val inflate = LayoutInflater.from(context).inflate(R.layout.pager_tutorial_01, parent, false)
                Page01ViewHolder(inflate)
            }

            1 -> {
                val inflate = LayoutInflater.from(context).inflate(R.layout.pager_tutorial_02, parent, false)
                Page02ViewHolder(inflate)
            }

            2 -> {
                val inflate = LayoutInflater.from(context).inflate(R.layout.pager_tutorial_03, parent, false)
                Page03ViewHolder(inflate)
            }

            else -> {
                val inflate = LayoutInflater.from(context).inflate(R.layout.pager_tutorial_01, parent, false)
                Page01ViewHolder(inflate)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is Page01ViewHolder -> {
                holder.pagerTutorial01NextButton.setOnClickListener {
                    viewPager2.setCurrentItem(1, true)
                }
            }

            is Page02ViewHolder -> {
                holder.pagerTutorial02AgreeCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    holder.pagerTutorial02NextButton.isEnabled = isChecked
                    if(isChecked) {
                        itemCount = 3
                        notifyItemInserted(2)
                    } else {
                        itemCount = 2
                        notifyItemRemoved(2)
                    }
                }

                holder.pagerTutorial02NextButton.setOnClickListener {
                    viewPager2.setCurrentItem(2, true)
                }
            }

            is Page03ViewHolder -> {
                holder.pagerTutorial03LoginButton.setOnClickListener {
                    activity.setResult(TutorialActivity.RESULT_CODE)
                    activity.finish()
                }
            }
        }
    }

}