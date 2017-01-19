package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, NoteActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val item = menu.add("Log out")
        item.setIcon(android.R.drawable.ic_menu_search)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // logout
        SApplication.instance!!.valueStore.setTokenAndMasterKey(null, null)
        startActivity(Intent(this, StarterActivity::class.java))
        // TODO clear caches and saved items
        finish()
        return true
    }




}
