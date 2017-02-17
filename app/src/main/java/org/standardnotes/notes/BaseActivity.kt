package org.standardnotes.notes

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast

open class BaseActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val KEY_ENABLE_SCREENSHOT = "enable_screenshots"
    lateinit var prefs: SharedPreferences

    protected val app: SApplication = SApplication.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (!isScreenshottingEnabled()) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            KEY_ENABLE_SCREENSHOT ->

                if (isScreenshottingEnabled()) enableScreenshots() else disableScreenshots()
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun disableScreenshots() {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)
        Toast.makeText(this, R.string.toast_screenshots_disabled, Toast.LENGTH_SHORT).show()
    }

    private fun enableScreenshots() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        Toast.makeText(this, R.string.toast_screenshots_enabled, Toast.LENGTH_SHORT).show()
    }

    private fun isScreenshottingEnabled(): Boolean {
        return prefs.getBoolean(KEY_ENABLE_SCREENSHOT, false)
    }
}
