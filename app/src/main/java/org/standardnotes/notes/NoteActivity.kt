package org.standardnotes.notes

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.frag.NoteFragment

/**
 * Created by carl on 15/01/17.
 */

class NoteActivity : AppCompatActivity() {

    var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = SApplication.instance!!.gson.fromJson(intent.extras.getString("note"), Note::class.java)
        title = note!!.title
        val frag: NoteFragment = NoteFragment()
        frag.arguments = intent.extras
        supportFragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()
    }

}