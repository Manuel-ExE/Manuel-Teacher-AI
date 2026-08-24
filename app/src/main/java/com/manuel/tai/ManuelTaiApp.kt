package com.manuel.tai

import android.app.Application
import com.manuel.tai.ui.ThemePrefs

class ManuelTaiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePrefs.apply(this)
    }
}
