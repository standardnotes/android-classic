package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class StarterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SApplication.instance.valueStore.token != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
