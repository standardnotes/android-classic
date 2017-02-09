package org.standardnotes.notes

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : AppCompatActivity() {

    companion object {
        const val IS_SCREENSHOTTING_ENABLED = "toggle_screenshots"
    }
    lateinit var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        editor = prefs.edit()

        if (!isScreenshottingEnabled()) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
        }

        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        title = getString(R.string.app_name)

        fab.setOnClickListener { view ->
            (supportFragmentManager.findFragmentById(R.id.noteListFrag) as NoteListFragment).startNewNote()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.logged_in, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (!isScreenshottingEnabled()) {
            menu?.findItem(R.id.toggleScreenshot)?.setTitle(R.string.action_enable_screenshots)
        } else {
            menu?.findItem(R.id.toggleScreenshot)?.setTitle(R.string.action_disable_screenshots)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> logout()
            R.id.toggleScreenshot -> if (isScreenshottingEnabled()) disableScreenshots() else enableScreenshots()
        }
        return true
    }

    private fun logout() {
        SApplication.instance!!.valueStore.setTokenAndMasterKey(null, null)
        SApplication.instance!!.noteStore.deleteAll()
        startActivity(Intent(this, StarterActivity::class.java))
        finish()
    }

    private fun disableScreenshots() {
        editor.putBoolean(IS_SCREENSHOTTING_ENABLED, false)
        editor.commit()
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)
        Toast.makeText(this, R.string.toast_screenshots_disabled, Toast.LENGTH_SHORT).show()
    }

    private fun enableScreenshots() {
        editor.putBoolean(IS_SCREENSHOTTING_ENABLED, true)
        editor.commit()
        finish()
        startActivity(intent)
        Toast.makeText(this, R.string.toast_screenshots_enabled, Toast.LENGTH_SHORT).show()
    }

    private fun isScreenshottingEnabled(): Boolean {
        return prefs.getBoolean(IS_SCREENSHOTTING_ENABLED, false)
    }
}
