package com.riku1227.viewrchat.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.system.ErrorHandling
import com.riku1227.viewrchat.util.SettingsUtil
import com.riku1227.vrchatlin.VRChatlin
import com.riku1227.vrchatlin.VRChatlinCookieJar

import kotlinx.android.synthetic.main.activity_web_view_login.*

class WebViewLoginActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 1546
        const val RESULT_CODE = 1898
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SettingsUtil.initBlackTheme(this)

        setContentView(R.layout.activity_web_view_login)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(loginActivityWebView, true)

        loginActivityWebView.settings.javaScriptEnabled = true
        loginActivityWebView.settings.loadWithOverviewMode = true
        loginActivityWebView.settings.useWideViewPort = true
        loginActivityWebView.setInitialScale(1)

        loginActivityWebView.loadUrl("https://www.vrchat.com/home/")
        loginActivityWebView.setBackgroundColor(0)

        loginActivityFAB.setOnClickListener {
            val cookie = cookieManager.getCookie("https://www.vrchat.com")

            if(cookie.indexOf("apiKey=") != -1 && cookie.indexOf("auth=authcookie") != -1) {
                val vrChatlin = VRChatlin.get(applicationContext)
                vrChatlin.APIService(ViewRChat.getVRChatCookiePreferences(baseContext))

                val cookieSplit = cookie.split(" ")
                for (i in cookieSplit) {
                    if(i.indexOf("apiKey=") != -1) {
                        vrChatlin.setCookie(VRChatlinCookieJar.COOKIE_API_KEY, "$i path=/")
                    } else if(i.indexOf("auth=") != -1) {
                        vrChatlin.setCookie(VRChatlinCookieJar.COOKIE_AUTH, "$i path=/")
                    }
                }
                ViewRChat.getGeneralPreferences(baseContext).edit().putBoolean("is_login", true).apply()
                ErrorHandling.isAuthErrorHandlingNow = false
                setResult(RESULT_CODE)
                finish()
            } else {
                Toast.makeText(baseContext, resources.getString(R.string.activity_login_please_login), Toast.LENGTH_LONG).show()
            }
        }
    }

}
