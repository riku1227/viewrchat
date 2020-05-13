package com.riku1227.viewrchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.AppLaunchChecker
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.db.CacheTimeDataDB
import com.riku1227.viewrchat.dialog.CrashReportDialog
import com.riku1227.viewrchat.system.CacheSystem
import com.riku1227.viewrchat.system.CrashDetection
import com.riku1227.vrchatlin.VRChatlin
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

            VRChatlin.get(applicationContext).APIService(ViewRChat.getVRChatCookiePreferences(applicationContext))

            val db = CacheTimeDataDB.getInstance(applicationContext)
            val cacheMap = db.readAllData()
            val nowTime = System.currentTimeMillis() / 1000

            for(item in cacheMap) {
                val diffTime = nowTime - item.cacheTime
                if(diffTime >= CacheSystem.DB_CLEAR_TIME) {
                    db.deleteData(item.id)
                    CacheSystem.deleteCacheFile(applicationContext, item.id, item.cacheType)
                }
            }


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
                    setupNavigation()
                } else {
                    val intent = Intent(this, WebViewLoginActivity::class.java)
                    startActivityForResult(intent, WebViewLoginActivity.REQUEST_CODE)
                }
            }
        }

        if(ViewRChat.getGeneralPreferences(baseContext).getBoolean("is_login", false)) {
            setupNavigation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            TutorialActivity.REQUEST_CODE -> {
                if(resultCode == TutorialActivity.RESULT_CODE) {
                    AppLaunchChecker.onActivityCreate(this)
                    val intent = Intent(this, WebViewLoginActivity::class.java)
                    startActivityForResult(intent, WebViewLoginActivity.REQUEST_CODE)
                } else {
                    finish()
                }
            }

            WebViewLoginActivity.REQUEST_CODE -> {
                if(resultCode == WebViewLoginActivity.RESULT_CODE) {
                    setupNavigation()
                } else {
                    finish()
                }
            }
        }
    }

    private fun setupNavigation() {
        val navHost = mainActivityFragment as NavHostFragment
        val navController = navHost.navController
        val graph = navController.navInflater.inflate(R.navigation.activity_main_navigation)
        navController.graph = graph
        NavigationUI.setupWithNavController(mainActivityBottomNavigation, navController)
    }
}
