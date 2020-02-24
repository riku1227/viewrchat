package com.riku1227.viewrchat.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.AppLaunchChecker
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.dialog.CrashReportDialog
import com.riku1227.viewrchat.system.CrashDetection
import com.riku1227.viewrchat.system.ErrorHandling
import com.riku1227.vrchatlin.VRChatlin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var compositeDisposable: CompositeDisposable? = null

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
        compositeDisposable = CompositeDisposable()
        val disposable = VRChatlin.get(applicationContext).APIService(ViewRChat.getVRChatCookiePreferences(baseContext))
            .getCurrentUserInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    textView.text =
"""ユーザー名: ${it.username}
bio: ${it.bio}
bioLinks: ${it.bioLinks}
homeLocation: ${it.homeLocation}
last_login: ${it.last_login}
last_platform: ${it.last_platform}
""".trimIndent()
                },
                {
                    ErrorHandling.onNetworkError(it, baseContext, this)
                }
            )
        compositeDisposable!!.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.dispose()
    }
}
