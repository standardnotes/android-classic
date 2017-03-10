package org.standardnotes.notes

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.frag_note_list.*
import kotlinx.android.synthetic.main.two_pane_container.*
import kotlinx.android.synthetic.main.view_navigation_header.view.*
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.frag.NoteFragment
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : BaseActivity(), SyncManager.SyncListener, NoteListFragment.OnNewNoteClickListener {

    override fun newNoteListener(uuid: String) {
        noteFragment = NoteFragment()
        val bundle = Bundle()
        bundle.putString(NoteListFragment.EXTRA_NOTE_ID, uuid)
        noteFragment!!.arguments = bundle
        if (note_container == null) {
            supportFragmentManager.beginTransaction().add(R.id.note_list_container, noteFragment, "note_fragment").addToBackStack(null).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.note_container, noteFragment, "note_fragment").commit()
        }
    }

    private lateinit var noteListFragment: NoteListFragment
    private var noteFragment: NoteFragment? = null

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var selectedTagId = ""

    override fun onSyncStarted() {
    }

    override fun onSyncFailed() {
        onSyncCompleted()
    }

    override fun onSyncCompleted() {
        updateTagsMenu()
        noteListFragment.refreshNotesForTag(selectedTagId)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString("tag", selectedTagId)
        supportFragmentManager.putFragment(outState, "note_list_fragment", noteListFragment)
        if (noteFragment != null) {
            supportFragmentManager.putFragment(outState, "note_fragment", noteFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && savedInstanceState?.containsKey("tag")) {
            selectedTagId = savedInstanceState.getString("tag")
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            noteListFragment = NoteListFragment()
            supportFragmentManager.beginTransaction().replace(R.id.note_list_container, noteListFragment, "note_list_fragment").addToBackStack(null).commit()
            supportFragmentManager.executePendingTransactions()
            if (note_container != null && noteListFragment.adapter.itemCount > 0) {
                noteListFragment.list_note.findViewHolderForAdapterPosition(0).itemView.performClick()
            }
        } else {
            noteListFragment = supportFragmentManager.findFragmentByTag("note_list_fragment") as NoteListFragment
            if (note_container != null) {
                noteFragment = supportFragmentManager.findFragmentByTag("note_fragment") as? NoteFragment
                if (noteFragment != null) {
                    supportFragmentManager.beginTransaction().remove(noteFragment).commit()
                    supportFragmentManager.executePendingTransactions()
                }
            } else {
                noteFragment = null
            }
        }

        noteListFragment.onNewNoteListener = this

        drawerToggle = ActionBarDrawerToggle(this, drawer_layout,  R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(drawerToggle!!)
        drawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val header = drawer.inflateHeaderView(R.layout.view_navigation_header)
        val values = SApplication.instance.valueStore
        header.main_account_server.text = values.server
        header.main_account_email.text = values.email

        title = getString(R.string.app_name)
    }

    fun updateTagsMenu() {
        fun tagMenuItem(it: MenuItem, uuid: String) {
            it.setIcon(R.drawable.ic_tag)
            it.setOnMenuItemClickListener {
                drawer_layout.closeDrawers()
                selectedTagId = uuid
                updateTagsMenu()
                noteListFragment.refreshNotesForTag(selectedTagId)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.logged_in, menu)
        if (BuildConfig.DEBUG) {
            menu.findItem(R.id.debug).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerToggle!!.onOptionsItemSelected(item))
            return true
        else
            when (item?.itemId) {
                R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.debug -> startActivity(Intent(this, DebugActivity::class.java))
            }
        return false
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
