package org.standardnotes.notes.frag

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frag_note.*
import kotlinx.android.synthetic.main.item_tag.view.*
import org.joda.time.DateTime
import org.standardnotes.notes.R
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.SyncItems
import org.standardnotes.notes.comms.data.Tag
import java.util.*

class NoteFragment : Fragment(), SyncManager.SyncListener {

    val SYNC_DELAY = 250L
    val syncHandler: Handler = Handler()
    val syncRunnable: Runnable = Runnable {
        if (saveNote(note)) {
            SyncManager.sync()
        }
    }

    var note: Note? = null
    var tags: List<Tag> = Collections.emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteUuid = arguments?.getString(NoteListFragment.EXTRA_NOTE_ID)
        if (noteUuid != null) {
            note = SApplication.instance!!.noteStore.getNote(noteUuid)
            tags = SApplication.instance!!.noteStore.getTagsForNote(noteUuid)
        } else {
            note = newNote()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NoteListFragment.EXTRA_NOTE_ID, note?.uuid)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.frag_note, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = activity as AppCompatActivity
        context.setSupportActionBar(toolbar)
        context.supportActionBar?.setDisplayShowHomeEnabled(true)
        context.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        if (prefs.getBoolean("notes_monospace", false))
            bodyEdit.typeface = Typeface.MONOSPACE

        populateUI()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                syncHandler.removeCallbacks(syncRunnable)
                syncHandler.postDelayed(syncRunnable, SYNC_DELAY)
            }

            override fun afterTextChanged(s: Editable?) {
                //
            }
        }
        titleEdit.addTextChangedListener(textWatcher)
        bodyEdit.addTextChangedListener(textWatcher)

        titleEdit.setSelection(titleEdit.text.length)

        setSubtitle(if (note!!.dirty) getString(R.string.sync_progress_error) else getString(R.string.sync_progress_finished))
    }

    override fun onResume() {
        super.onResume()
        SyncManager.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        syncHandler.removeCallbacks(syncRunnable)
        SyncManager.unsubscribe(this)
        saveNote(note)
    }


    fun setSubtitle(subTitle: String) {
        bodyEdit.postDelayed({
            (activity as AppCompatActivity).supportActionBar!!.subtitle = subTitle
        }, 100)
    }

    fun populateUI() {
        titleEdit.setText(note?.title)
        bodyEdit.setText(note?.text)
        if (tags.count() > 0) {
            tagsLayout.removeAllViews()
            tagsRow.visibility = View.VISIBLE
            tags.forEach {
                val tagItem = LayoutInflater.from(activity).inflate(R.layout.item_tag, tagsLayout, false)
                tagItem.tagText.text = it.title
                tagsLayout.addView(tagItem)
            }
        } else {
            tagsRow.visibility = View.GONE
        }
        if (activity.currentFocus == titleEdit)
            titleEdit.setSelection(titleEdit.text.length)
        else if (activity.currentFocus == bodyEdit)
            bodyEdit.setSelection(bodyEdit.text.length)
    }

    fun saveNote(note: Note?): Boolean {
        if (note!!.title != titleEdit.text.toString() ||
                note.text != bodyEdit.text.toString()) {
            note.title = titleEdit.text.toString()
            note.text = bodyEdit.text.toString()
            note.dirty = true
            note.updatedAt = DateTime.now()
            SApplication.instance!!.noteStore.putNote(note.uuid, note)
            return true
        }
        return false
    }

    override fun onSyncStarted() {
        setSubtitle(getString(R.string.sync_progress_saving))
    }

    override fun onSyncCompleted(syncItems: SyncItems) {
        setSubtitle(getString(R.string.sync_progress_finished))
        for (retrievedItem in syncItems.retrievedItems) {
            if (retrievedItem.uuid == note?.uuid) {
                if (retrievedItem.updatedAt > note?.updatedAt) {
                    note = SApplication.instance!!.noteStore.getNote(retrievedItem.uuid)
                    tags = SApplication.instance!!.noteStore.getTagsForNote(retrievedItem.uuid)
                    populateUI()
                }
                break
            }
        }
    }

    override fun onSyncFailed() {
        setSubtitle(getString(R.string.sync_progress_error))
    }

}

fun newNote(): Note {
    // Move to a factory
    val note = Note()
    note.uuid = UUID.randomUUID().toString()
    note.encItemKey = Crypt.generateEncryptedKey(512)
    note.createdAt = DateTime.now()
    note.updatedAt = note.createdAt
    return note
}
