package com.riku1227.viewrchat.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(settingsActivityToolbar)
        if(savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settingsActivityFrameLayout, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartScreen(
        caller: PreferenceFragmentCompat?,
        pref: PreferenceScreen?
    ): Boolean {
        pref?.let {
            when(it.key) {
                "preferences_appearance" -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.settingsActivityFrameLayout, SettingsFragment.newInstance(R.xml.appearance_preferences))
                        .addToBackStack(null)
                        .commit()
                }
                else -> {}
            }
        }
        return true
    }
}