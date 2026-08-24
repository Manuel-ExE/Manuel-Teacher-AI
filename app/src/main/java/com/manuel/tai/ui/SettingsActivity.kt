package com.manuel.tai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.manuel.tai.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val currentOptionId = when (ThemePrefs.currentMode(this)) {
            ThemePrefs.MODE_LIGHT -> binding.themeLightOption.id
            ThemePrefs.MODE_DARK -> binding.themeDarkOption.id
            else -> binding.themeSystemOption.id
        }
        binding.themeRadioGroup.check(currentOptionId)

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                binding.themeLightOption.id -> ThemePrefs.MODE_LIGHT
                binding.themeDarkOption.id -> ThemePrefs.MODE_DARK
                else -> ThemePrefs.MODE_SYSTEM
            }
            // AppCompatDelegate recreates visible activities automatically,
            // so the dashboard behind this screen updates as soon as the
            // teacher backs out.
            ThemePrefs.setMode(this, mode)
        }
    }
}
