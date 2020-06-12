package com.riku1227.viewrchat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.riku1227.viewrchat.R
import com.riku1227.viewrchat.util.SettingsUtil

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
        }
    }

    private fun onCreateMainPreferences() {
        val appearancePreferences: Preference? = findPreference("preferences_appearance")
        appearancePreferences?.let {
            it.setOnPreferenceClickListener {
                addFragment(R.xml.appearance_preferences)
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

    private fun addFragment(xmlID: Int) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.settingsActivityFrameLayout, newInstance(xmlID))
            ?.addToBackStack(null)?.commit()
    }
}