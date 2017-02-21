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
import org.standardnotes.notes.comms.data.SyncItems
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : BaseActivity(), SyncManager.SyncListener {

    override fun onSyncStarted() {
    }

    override fun onSyncFailed() {
    }

    override fun onSyncCompleted(syncItems: SyncItems) {
        // Update tags list
        updateTagsMenu()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString("tag", selectedTag)
    }

    private var drawerToggle: ActionBarDrawerToggle? = null
    private var selectedTag = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && savedInstanceState?.containsKey("tag")) {
            selectedTag = savedInstanceState.getString("tag")
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawer_layout,  R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(drawerToggle!!)
        drawerToggle!!.isDrawerIndicatorEnabled = true
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        val header = drawer.inflateHeaderView(R.layout.view_navigation_header)
        val values = SApplication.instance.valueStore
        header.main_account_server.text = values.server
        header.main_account_email.text = values.email

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
            noteListFragment().startNewNote(lastX!!, lastY!!, selectedTag)
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
                selectedTag = uuid
                updateTagsMenu()
                noteListFragment().refreshNotesForTag(selectedTag)
                return@setOnMenuItemClickListener true
            }
        }
        drawer.menu.clear()
        drawer.inflateMenu(R.menu.drawer_tags)
        val tags = SApplication.instance.noteStore.getAllTags()
        val menu = drawer.menu.findItem(R.id.menu_account_tags).subMenu
        var allNotes = menu.add(getString(R.string.drawer_all_notes))
        var selectedUUID = ""
        tagMenuItem(allNotes, "")
        var selected: MenuItem = allNotes
        for (tag in tags) {
            val item = menu.add(tag.title)
            if (selectedTag.equals(tag.uuid)) {
                selected = item
                selectedUUID = tag.uuid
            }
            tagMenuItem(item, tag.uuid)
        }
        selected.isChecked = true
        toolbar.subtitle = selected.title
        selectedTag = selectedUUID // In case selected tag wasn't found in list
    }

    override fun onResume() {
        super.onResume()
        SyncManager.subscribe(this)
        updateTagsMenu()
        noteListFragment().refreshNotesForTag(selectedTag)
    }

    override fun onPause() {
        super.onPause()
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
