package com.riku1227.viewrchat.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.AppLaunchChecker
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.dialog.CrashReportDialog
import com.riku1227.viewrchat.fragment.FriendsLocationFragment
import com.riku1227.viewrchat.system.CrashDetection
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainActivityToolbar)

        if(savedInstanceState == null) {
            val crashDetection = CrashDetection(baseContext)
            Thread.setDefaultUncaughtExceptionHandler(crashDetection)


            val generalPreference = ViewRChat.getGeneralPreferences(baseContext)
            val crashLog = generalPreference.getString("crash_log", "")!!

            if(crashLog != "") {
                val reportDialog = CrashReportDialog(crashLog)
                reportDialog.show(supportFragmentManager, "CrashReportDialog")
                generalPreference.edit().putString("crash_log", "").apply()
            }

            if(!AppLaunchChecker.hasStartedFromLauncher(applicationContext)) {
                val intent = Intent(this, TutorialActivity::class.java)
                startActivityForResult(intent, TutorialActivity.REQUEST_CODE)
            } else {
                if(ViewRChat.getGeneralPreferences(baseContext).getBoolean("is_login", false)) {
                    setup()
                } else {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(intent, LoginActivity.REQUEST_CODE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            TutorialActivity.REQUEST_CODE -> {
                if(resultCode == TutorialActivity.RESULT_CODE) {
                    AppLaunchChecker.onActivityCreate(this)
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivityForResult(intent, LoginActivity.REQUEST_CODE)
                } else {
                    finish()
                }
            }

            LoginActivity.REQUEST_CODE -> {
                if(resultCode == LoginActivity.RESULT_CODE) {
                    setup()
                } else {
                    finish()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setup() {
        supportFragmentManager.beginTransaction().add(R.id.mainActivityFrameLayout, FriendsLocationFragment()).commit()
    }
}
