package com.riku1227.viewrchat.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.adapter.TutorialRecyclerAdapter

import kotlinx.android.synthetic.main.activity_tutorial.*

class TutorialActivity : AppCompatActivity() {

    companion object {
        const val TUTORIAL_ACTIVITY_RESULT = 5839
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        tutorialActivityViewPager2.adapter = TutorialRecyclerAdapter(baseContext, tutorialActivityViewPager2, this)
    }

}
