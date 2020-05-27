package com.riku1227.viewrchat

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.riku1227.viewrchat.util.RadiusOutlineProvider

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
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        when(preferences.getString("preferences_appearance_theme_key", "")) {
            "follow_system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "light_theme" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark_theme" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}