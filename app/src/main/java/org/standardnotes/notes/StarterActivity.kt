package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.standardnotes.notes.frag.EXTRA_TEXT

class StarterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(mainIntent)
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            var text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (text != null) {
                val intent = Intent(this, NoteActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(EXTRA_TEXT, text)
                startActivity(intent)
            }
        }
        finish()
    }
}
