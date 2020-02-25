package com.riku1227.viewrchat.system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.activity.WebViewLoginActivity
import retrofit2.HttpException

class ErrorHandling {
    companion object {
        var isAuthErrorHandlingNow = false
        fun onNetworkError(throwable: Throwable, context: Context, activity: Activity? = null, fragment: Fragment? = null) {
            if(throwable is HttpException) {
                when (throwable.code()) {
                    401 -> {
                        if(!isAuthErrorHandlingNow) {
                            val intent = Intent(context, WebViewLoginActivity::class.java)
                            Toast.makeText(context, context.resources.getString(R.string.general_please_re_login), Toast.LENGTH_LONG).show()
                            isAuthErrorHandlingNow = true
                            activity?.startActivityForResult(intent, WebViewLoginActivity.REQUEST_CODE)
                            fragment?.startActivityForResult(intent, WebViewLoginActivity.REQUEST_CODE)
                        }
                    }
                }
            }
        }
    }
}