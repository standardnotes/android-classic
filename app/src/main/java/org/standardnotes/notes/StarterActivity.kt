package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.standardnotes.notes.frag.EXTRA_TEXT

class StarterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SApplication.instance.valueStore.token != null) {
            startActivity(Intent(this, MainActivity::class.java))
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                var text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (text != null) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(EXTRA_TEXT, text)
                    startActivity(intent)
                }
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
