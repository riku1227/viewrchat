package com.riku1227.viewrchat.util

import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.riku1227.viewrchat.R

class SettingsUtil {
    companion object {
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