package com.riku1227.viewrchat.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.riku1227.viewrchat.R

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        fun newInstance(xmlId: Int): PreferenceFragmentCompat {
            val fragment = SettingsFragment()
            val bundle = Bundle()
            bundle.putInt("preference_xml_id", xmlId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var xmlId = R.xml.root_preferences
        arguments?.let {
            xmlId = it.getInt("preference_xml_id", R.xml.root_preferences)
        }

        setPreferencesFromResource(xmlId, rootKey)

        when (xmlId) {
            R.xml.appearance_preferences -> onCreateAppearancePreferences()
        }
    }

    private fun onCreateAppearancePreferences() {
        val themePreferences: ListPreference? = findPreference("preferences_appearance_theme_key")
        themePreferences?.let {
            it.setOnPreferenceChangeListener { _, newValue ->
                when(newValue.toString()) {
                    "follow_system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "light_theme" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "dark_theme" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                return@setOnPreferenceChangeListener true
            }
        }
    }
}