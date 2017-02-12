package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        title = getString(R.string.app_name)

        var lastX: Int? = null
        var lastY: Int? = null
        fab.setOnTouchListener({ v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
            }
            false
        })
        fab.setOnClickListener { view ->
            (supportFragmentManager.findFragmentById(R.id.noteListFrag) as NoteListFragment).startNewNote(lastX!!, lastY!!)
        }

    }

    override fun onResume() {
        super.onResume()
        SyncManager.startSyncTimer()
    }

    override fun onPause() {
        super.onPause()
        SyncManager.stopSyncTimer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.logged_in, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }


}
