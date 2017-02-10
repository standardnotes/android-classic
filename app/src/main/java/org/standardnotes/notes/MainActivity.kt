package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        title = getString(R.string.app_name)

        fab.setOnClickListener { view ->
            (supportFragmentManager.findFragmentById(R.id.noteListFrag) as NoteListFragment).startNewNote()
        }

        SyncManager.startSyncTimer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val item = menu.add(getString(R.string.action_logout))
        item.setIcon(android.R.drawable.ic_menu_search)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // only item is logout
        SApplication.instance!!.valueStore.setTokenAndMasterKey(null, null)
        SApplication.instance!!.noteStore.deleteAll()
        SyncManager.stopSyncTimer()
        startActivity(Intent(this, StarterActivity::class.java))
        finish()
        return true
    }
}
