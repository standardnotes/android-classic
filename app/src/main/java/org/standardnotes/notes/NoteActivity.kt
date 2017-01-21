package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.WindowManager
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.frag.NoteFragment

/**
 * Created by carl on 15/01/17.
 */

class NoteActivity : AppCompatActivity() {

    var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)
        note = SApplication.instance!!.gson.fromJson(intent.getStringExtra("note"), Note::class.java)
        title = note?.title ?: "New note"
        val frag: NoteFragment = NoteFragment()
        frag.arguments = intent.extras
        supportFragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()
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