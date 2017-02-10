package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.view.MenuItem
import org.standardnotes.notes.frag.NoteFragment

class NoteActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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