package org.standardnotes.notes

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_navigation_header.view.*
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : BaseActivity(), SyncManager.SyncListener {

    override fun onSyncStarted() {
    }

    override fun onSyncFailed() {
    }

    override fun onSyncCompleted(notes: List<Note>) {
        // Update tags list
        updateTagsMenu()
    }

    private var drawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawer_layout,  R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(drawerToggle!!)
        drawerToggle!!.isDrawerIndicatorEnabled = true
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        val header = drawer.inflateHeaderView(R.layout.view_navigation_header)
        val values = SApplication.instance!!.valueStore
        header.main_account_server.text = values.server
        header.main_account_email.text = values.email

        title = getString(R.string.app_name)

        fab.setOnClickListener { view ->
            (supportFragmentManager.findFragmentById(R.id.noteListFrag) as NoteListFragment).startNewNote()
        }

    }

    fun updateTagsMenu() {
        fun tagMenuItem(it: MenuItem, uuid: String?) {
            // TODO: pass UUID to Fragment
            it.setIcon(R.drawable.ic_tag)
            it.setOnMenuItemClickListener {
                drawer_layout.closeDrawers()
                return@setOnMenuItemClickListener true
            }
        }
        drawer.menu.clear()
        drawer.inflateMenu(R.menu.drawer_tags)
        val tags = SApplication.instance!!.noteStore.getAllTags()
        val mitem = drawer.menu.findItem(R.id.menu_account_tags)
        val item = mitem.subMenu.add(getString(R.string.drawer_all_notes))
        tagMenuItem(item, null)
        for (tag in tags) {
            val item = mitem.subMenu.add(tag.title)
            tagMenuItem(item, tag.uuid)
        }

    }

    override fun onResume() {
        super.onResume()
        SyncManager.startSyncTimer()
        SyncManager.subscribe(this)
        updateTagsMenu()
    }

    override fun onPause() {
        super.onPause()
        SyncManager.stopSyncTimer()
        SyncManager.unsubscribe(this)
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
        if (drawerToggle!!.onOptionsItemSelected(item))
            return true
        else
            when (item?.itemId) {
                R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.syncState()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle?.syncState()
    }

}
