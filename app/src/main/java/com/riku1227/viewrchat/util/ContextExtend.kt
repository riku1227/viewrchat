package com.riku1227.viewrchat.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

fun Context.getVersionName(): String {
    var result = ""
    try {
        val pi = this.packageManager.getPackageInfo(this.packageName, 0)
        result = pi.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        Log.d("ViewRChat", "getVersionName: $e")
    }
    return result
}