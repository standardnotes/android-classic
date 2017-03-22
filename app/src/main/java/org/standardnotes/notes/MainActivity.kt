package org.standardnotes.notes

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_navigation_header.view.*
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.frag.NoteFragment
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : BaseActivity(), SyncManager.SyncListener, NoteListFragment.OnNewNoteClickListener, NoteFragment.DetachListener {

    val TAG_NOTE_FRAGMENT = "note_fragment"
    val TAG_NOTE_LIST_FRAGMENT = "note_list_fragment"
    val EXTRA_TAG = "tag"

    private lateinit var noteListFragment: NoteListFragment
    private var noteFragment: NoteFragment? = null

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var selectedTagId = ""

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString(EXTRA_TAG, selectedTagId)
        supportFragmentManager.putFragment(outState, TAG_NOTE_LIST_FRAGMENT, noteListFragment)
        if (supportFragmentManager.findFragmentByTag(TAG_NOTE_FRAGMENT) != null ) {
            supportFragmentManager.putFragment(outState, TAG_NOTE_FRAGMENT, noteFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && savedInstanceState?.containsKey(EXTRA_TAG)) {
            selectedTagId = savedInstanceState.getString(EXTRA_TAG)
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawer_layout,  R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(drawerToggle!!)
        addDrawerToggle()

        if (savedInstanceState == null) {
            noteListFragment = NoteListFragment()
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.master_container, noteListFragment, TAG_NOTE_LIST_FRAGMENT)
                    .commit()
        } else {
            noteListFragment = supportFragmentManager.findFragmentByTag(TAG_NOTE_LIST_FRAGMENT) as NoteListFragment
            noteFragment = supportFragmentManager.findFragmentByTag(TAG_NOTE_FRAGMENT) as? NoteFragment
            noteFragment?.detachListener = this

            if (noteFragment != null) {
                supportFragmentManager
                        .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager
                        .beginTransaction()
                        .remove(noteFragment)
                        .commitNow()

                if (findViewById(R.id.detail_container) != null) {
                    supportFragmentManager
                            .beginTransaction()
                            .remove(noteListFragment)
                            .commitNow()

                    supportFragmentManager
                            .beginTransaction()
                            .add(R.id.master_container, noteListFragment, TAG_NOTE_LIST_FRAGMENT)
                            .replace(R.id.detail_container, noteFragment, TAG_NOTE_FRAGMENT)
                            .commit()

                } else {

                    removeDrawerToggle()
                    supportFragmentManager
                            .beginTransaction()
                            .add(R.id.master_container, noteFragment, TAG_NOTE_FRAGMENT)
                            .addToBackStack(null)
                            .commit()
                }
            }
        }

        noteListFragment.onNewNoteListener = this

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

    fun removeDrawerToggle() {
        drawerToggle.isDrawerIndicatorEnabled = false
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(false)
    }

    override fun addDrawerToggle() {
        drawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun newNoteListener(uuid: String) {
        noteFragment = NoteFragment()
        noteFragment!!.detachListener = this
        val bundle = Bundle()
        bundle.putString(NoteListFragment.EXTRA_NOTE_ID, uuid)
        noteFragment!!.arguments = bundle
        if (findViewById(R.id.detail_container) == null) {
            removeDrawerToggle()
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.master_container, noteFragment, TAG_NOTE_FRAGMENT)
                    .addToBackStack(null)
                    .commit()
        } else {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.detail_container, noteFragment, TAG_NOTE_FRAGMENT)
                    .commit()
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

    override fun onSyncStarted() {
    }

    override fun onSyncFailed() {
        onSyncCompleted()
    }

    override fun onSyncCompleted() {
        updateTagsMenu()
        noteListFragment.refreshNotesForTag(selectedTagId)
    }
}
