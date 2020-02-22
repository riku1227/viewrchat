package com.riku1227.viewrchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.AppLaunchChecker
import com.riku1227.viewrchat.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainActivityToolbar)

        if(!AppLaunchChecker.hasStartedFromLauncher(applicationContext)) {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivityForResult(intent, TutorialActivity.TUTORIAL_ACTIVITY_RESULT)
        } else {
        }
    }
}
