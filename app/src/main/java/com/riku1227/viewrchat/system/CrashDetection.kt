package com.riku1227.viewrchat.system

import android.annotation.SuppressLint
import android.content.Context
import com.riku1227.viewrchat.ViewRChat
import java.io.PrintWriter
import java.io.StringWriter

class CrashDetection (val context: Context): Thread.UncaughtExceptionHandler {
    private val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    @SuppressLint("ApplySharedPref")
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val preference = ViewRChat.getGeneralPreferences(context)
        val editor = preference.edit()

        val stringWriter = StringWriter()
        e?.printStackTrace(PrintWriter(stringWriter))
        val stackTrace =  stringWriter.toString()

        editor.putString("crash_log", stackTrace).commit()

        defaultUncaughtExceptionHandler?.uncaughtException(t!!, e!!)
    }
}