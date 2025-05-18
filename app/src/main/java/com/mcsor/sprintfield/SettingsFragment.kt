package com.mcsor.sprintfield

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchNotifications: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchDarkMode: Switch
    private lateinit var resetSettingsButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        resetSettingsButton = view.findViewById(R.id.resetSettingsButton)

        loadSettings()

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("notifications_enabled", isChecked)

            val statusResId = if (isChecked) R.string.enabled else R.string.disabled
            val status = getString(statusResId)
            val message = getString(R.string.notifications_status, status)

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }


        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("dark_mode_enabled", isChecked)

            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            AppCompatDelegate.setDefaultNightMode(mode)

            val statusResId = if (isChecked) R.string.enabled else R.string.disabled
            val status = getString(statusResId)
            val message = getString(R.string.dark_mode_status, status)

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }


        resetSettingsButton.setOnClickListener {
            resetSettings()
            Toast.makeText(requireContext(), getString(R.string.settings_reset), Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadSettings() {
        val prefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)
        val darkModeEnabled = prefs.getBoolean("dark_mode_enabled", false)
        switchDarkMode.isChecked = darkModeEnabled

        val mode =
            if (darkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }


    private fun saveSetting(key: String, value: Boolean) {
        val prefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
    }

    private fun resetSettings() {
        val prefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        loadSettings()
    }
}
