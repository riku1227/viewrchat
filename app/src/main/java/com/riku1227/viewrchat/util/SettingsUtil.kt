package com.riku1227.viewrchat.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.riku1227.viewrchat.R

class SettingsUtil {
    companion object {
        fun initDayNightTheme(context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            setDayNightTheme(preferences.getString("preferences_appearance_theme_key", "")!!)
        }

        fun setDayNightTheme(value: String) {
            when(value) {
                "follow_system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                "light_theme" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark_theme" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        fun initBlackTheme(activity: AppCompatActivity) {
            activity.let {
                val preferences = PreferenceManager.getDefaultSharedPreferences(it.applicationContext)
                val isBlackTheme = preferences.getBoolean("preferences_appearance_enable_black_theme_key", false)
                if(isBlackTheme) {
                    it.setTheme(R.style.AppTheme_Black)
                }
            }
        }
    }
}