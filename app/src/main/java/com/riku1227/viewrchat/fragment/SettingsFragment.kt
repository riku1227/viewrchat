package com.riku1227.viewrchat.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.util.SettingsUtil
import com.riku1227.viewrchat.util.getVersionName


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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.let {
            if(SettingsUtil.isBlackTheme(requireContext()) && SettingsUtil.isDarkTheme(requireContext())) {
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var xmlId = R.xml.root_preferences
        arguments?.let {
            xmlId = it.getInt("preference_xml_id", R.xml.root_preferences)
        }

        setPreferencesFromResource(xmlId, rootKey)

        when (xmlId) {
            R.xml.root_preferences -> onCreateMainPreferences()
            R.xml.appearance_preferences -> onCreateAppearancePreferences()
            R.xml.app_info_preferences -> onCreateAppInfoPreferences()
        }
    }

    private fun onCreateMainPreferences() {
        val context = requireContext()
        val appearancePreferences: Preference? = findPreference("preferences_appearance")
        appearancePreferences?.let {
            it.setOnPreferenceClickListener {
                addFragment(R.xml.appearance_preferences)
                return@setOnPreferenceClickListener true
            }
        }

        val appInfoPreference: Preference? = findPreference("preferences_app_info")
        appInfoPreference?.let {
            it.summary = context.resources.getString(R.string.activity_settings_app_info_summary, context.getVersionName())

            it.setOnPreferenceClickListener {
                addFragment(R.xml.app_info_preferences)
                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun onCreateAppearancePreferences() {
        val themePreferences: ListPreference? = findPreference("preferences_appearance_theme_key")
        themePreferences?.let {
            it.setOnPreferenceChangeListener { _, newValue ->
                SettingsUtil.setDayNightTheme(newValue.toString())
                return@setOnPreferenceChangeListener true
            }
        }
    }

    private fun onCreateAppInfoPreferences() {
        val context = requireContext()
        findPreference<Preference>("preferences_app_info_version")?.let {
            it.summary = context.getVersionName()
        }

        findPreference<Preference>("preferences_app_info_source_code")?.let {
            it.setOnPreferenceClickListener {
                val uri = Uri.parse("https://github.com/riku1227/viewrchat")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                this.startActivity(intent)
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("preferences_app_info_twitter")?.let {
            it.setOnPreferenceClickListener {
                val uri = Uri.parse("https://twitter.com/_riku1227_")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                this.startActivity(intent)
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("preferences_app_info_website")?.let {
            it.setOnPreferenceClickListener {
                val uri = Uri.parse("https://riku1227.com/")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                this.startActivity(intent)
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("preferences_app_info_oss_licence")?.let {
            it.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))

                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun addFragment(xmlID: Int) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.settingsActivityFrameLayout, newInstance(xmlID))
            ?.addToBackStack(null)?.commit()
    }
}