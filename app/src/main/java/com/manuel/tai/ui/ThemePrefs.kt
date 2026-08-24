package com.manuel.tai.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Stores the teacher's Light / Dark / System theme choice and applies it via
 * [AppCompatDelegate]. Reading and applying this as early as possible (see
 * [com.manuel.tai.ManuelTaiApp]) avoids a visible flash of the wrong theme
 * on launch; calling [apply] again after a change recreates any visible
 * activities in the new mode automatically.
 */
object ThemePrefs {

    private const val PREFS_NAME = "manueltai_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    fun currentMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    fun setMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME_MODE, mode)
            .apply()
        applyMode(mode)
    }

    /** Reads the saved preference and applies it. Safe to call at app startup. */
    fun apply(context: Context) {
        applyMode(currentMode(context))
    }

    private fun applyMode(mode: Int) {
        val nightMode = when (mode) {
            MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
