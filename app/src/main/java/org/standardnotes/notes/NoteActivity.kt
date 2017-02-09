package org.standardnotes.notes

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.WindowManager
import org.standardnotes.notes.frag.NoteFragment

class NoteActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (!prefs.getBoolean(MainActivity.IS_SCREENSHOTTING_ENABLED, false)) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
        }

        //title = note?.title ?: "New note"
        if (savedInstanceState == null) {
            val frag: NoteFragment = NoteFragment()
            frag.arguments = intent.extras
            supportFragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()
        }
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (NavUtils.getParentActivityIntent(this) != null) {
                NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            } else {
                onBackPressed()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}