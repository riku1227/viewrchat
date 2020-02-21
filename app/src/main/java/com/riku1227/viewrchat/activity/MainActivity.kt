package com.riku1227.viewrchat.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.riku1227.viewrchat.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainActivityToolbar)
    }
}
