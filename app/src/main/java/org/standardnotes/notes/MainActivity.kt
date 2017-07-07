package org.standardnotes.notes

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_navigation_header.view.*
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : BaseActivity(), SyncManager.SyncListener {

    override fun onSyncStarted() {
    }

    override fun onSyncFailed() {
        onSyncCompleted()
    }

    override fun onSyncCompleted() {
        updateTagsMenu() // Update tags list
        noteListFragment().refreshNotesForTag(selectedTagId) // Update notes in fragment
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString("tag", selectedTagId)
    }


    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var selectedTagId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && savedInstanceState.containsKey("tag")) {
            selectedTagId = savedInstanceState.getString("tag")
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawer_layout, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val header = drawer.inflateHeaderView(R.layout.view_navigation_header)
        val values = SApplication.instance.valueStore
        if (values.token != null) {
            header.main_account_server.text = values.server
            header.main_account_email.text = values.email
            header.main_account_title.setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        } else {
            header.main_account_server.text = getText(R.string.not_logged_in)
            header.main_account_email.text = ""
            header.main_account_title.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

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
            noteListFragment().startNewNote(lastX!!, lastY!!, selectedTagId)
        }

    }

    fun noteListFragment(): NoteListFragment {
        return supportFragmentManager.findFragmentById(R.id.noteListFrag) as NoteListFragment
    }

    fun updateTagsMenu() {
        fun tagMenuItem(it: MenuItem, uuid: String) {
            it.setIcon(R.drawable.ic_tag)
            it.setOnMenuItemClickListener {
                drawer_layout.closeDrawers()
                selectedTagId = uuid
                updateTagsMenu()
                noteListFragment().refreshNotesForTag(selectedTagId)
                return@setOnMenuItemClickListener true
            }
        }
        drawer.menu.clear()
        drawer.inflateMenu(R.menu.drawer_tags)
        val tags = SApplication.instance.noteStore.getAllTags(false)
        val menu = drawer.menu.findItem(R.id.menu_account_tags).subMenu
        var allNotes = menu.add(getString(R.string.drawer_all_notes))
        var selectedId = ""
        tagMenuItem(allNotes, "")
        var selected: MenuItem = allNotes
        for (tag in tags) {
            val item = menu.add(tag.title)
            if (selectedTagId.equals(tag.uuid)) {
                selected = item
                selectedId = tag.uuid
            }
            tagMenuItem(item, tag.uuid)
        }
        selected.isChecked = true
        toolbar.subtitle = selected.title
        selectedTagId = selectedId // In case selected tag wasn't found in list
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
        if (BuildConfig.DEBUG) {
            menu.findItem(R.id.debug).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerToggle.onOptionsItemSelected(item))
            return true
        else
            when (item?.itemId) {
                R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.search-> startActivity(Intent(this, SearchActivity::class.java))
                R.id.debug -> startActivity(Intent(this, DebugActivity::class.java))
            }
        return true
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.syncState()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

}
