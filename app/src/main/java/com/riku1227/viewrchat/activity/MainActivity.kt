package com.riku1227.viewrchat.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.AppLaunchChecker
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
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

        if(!AppLaunchChecker.hasStartedFromLauncher(applicationContext)) {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivityForResult(intent, TutorialActivity.TUTORIAL_ACTIVITY_RESULT)
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
            TutorialActivity.TUTORIAL_ACTIVITY_RESULT -> {
                AppLaunchChecker.onActivityCreate(this)
                val intent = Intent(this, LoginActivity::class.java)
                startActivityForResult(intent, LoginActivity.REQUEST_CODE)
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
                    Log.d("ViewRChat", "$it")
                }
            )
        compositeDisposable!!.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.dispose()
    }
}
