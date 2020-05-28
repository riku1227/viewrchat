package com.riku1227.viewrchat

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.riku1227.viewrchat.util.RadiusOutlineProvider
import com.riku1227.viewrchat.util.SettingsUtil

class ViewRChat: Application() {
    companion object {
        val imageRadiusOutlineProvider = RadiusOutlineProvider(8F)

        fun getVRChatCookiePreferences(context: Context): SharedPreferences {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            return EncryptedSharedPreferences.create(
                "vrchat_cookie",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        fun getGeneralPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences("general_preference", Context.MODE_PRIVATE)
        }
    }

    override fun onCreate() {
        super.onCreate()
        SettingsUtil.initDayNightTheme(applicationContext)
    }
}