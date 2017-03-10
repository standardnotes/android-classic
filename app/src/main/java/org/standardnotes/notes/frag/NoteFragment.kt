package org.standardnotes.notes.frag

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.frag_note.*
import kotlinx.android.synthetic.main.item_tag_lozenge.view.*
import org.joda.time.DateTime
import org.standardnotes.notes.*
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.SyncManager
import org.standardnotes.notes.comms.data.ContentType
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.Reference
import org.standardnotes.notes.comms.data.Tag
import java.util.*

const val REQ_TAGS = 1

val EXTRA_TEXT = "text"

class NoteFragment : Fragment(), SyncManager.SyncListener {

    val SYNC_DELAY = 250L
    val syncHandler: Handler = Handler()
    val syncRunnable: Runnable = Runnable {
        if (saveNote()) {
            SyncManager.sync()
        }
    }

    lateinit var note: Note
    lateinit var tags: List<Tag>
    lateinit var mActivity: MainActivity
    lateinit var detachListener: DetachListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.frag_note, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteUuid =
                savedInstanceState?.getString(NoteListFragment.EXTRA_NOTE_ID) ?:
                        arguments?.getString(NoteListFragment.EXTRA_NOTE_ID)
        if (noteUuid != "") {
            note = SApplication.instance.noteStore.getNote(noteUuid!!)!!
            tags = SApplication.instance.noteStore.getTagsForNote(noteUuid)
        } else {
            note = newNote()
            val tagUUID = arguments?.getString(NoteListFragment.EXTRA_TAG_ID)
            val tag = if (tagUUID != null)
                SApplication.instance.noteStore.getTag(tagUUID)
            else
                null
            if (tag != null) {
                tags = arrayListOf(tag)
            } else {
                tags = Collections.emptyList()
            }
            val text = arguments?.getString(EXTRA_TEXT)
            if (text != null) {
                bodyEdit.setText(text)
                syncRunnable.run()
            }
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        if (prefs.getBoolean("notes_monospace", false))
            bodyEdit.typeface = Typeface.MONOSPACE
        titleEdit.setText(note.title)
        bodyEdit.setText(note.text)

        if (tags.count() > 0) {
            tagsRow.visibility = View.VISIBLE
            tags.forEach {
                val tagItem = LayoutInflater.from(activity).inflate(R.layout.item_tag_lozenge, tagsLayout, false)
                tagItem.tagText.text = it.title
                tagsLayout.addView(tagItem)
            }
        }

        updateTags()

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
        setSubtitle(if (note.dirty) getString(R.string.sync_progress_error) else getString(R.string.sync_progress_finished))
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = (context as MainActivity)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NoteListFragment.EXTRA_NOTE_ID, note.uuid)
    }

    private fun updateTags() {
        if (tags.count() > 0) {
            tagsRow.visibility = View.VISIBLE
            tagsLayout.removeAllViews()
            tags.forEach {
                val tagItem = LayoutInflater.from(activity).inflate(R.layout.item_tag_lozenge, tagsLayout, false)
                (tagItem.findViewById(R.id.tagText) as TextView).text = it.title
                tagsLayout.addView(tagItem)
            }
        } else {
            tagsRow.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        SyncManager.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        syncHandler.removeCallbacks(syncRunnable)
        SyncManager.unsubscribe(this)
        syncRunnable.run()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_TAGS && data != null) {
            val listType = object : TypeToken<List<Tag>>() {}.type
            tags = SApplication.Companion.instance.gson.fromJson(data.getStringExtra(EXTRA_TAGS), listType)
            updateTags()
            syncRunnable.run()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.tags -> {
                val intent = Intent(activity, TagListActivity::class.java)
                intent.putExtra(EXTRA_TAGS, SApplication.Companion.instance.gson.toJson(tags))
                startActivityForResult(intent, REQ_TAGS)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setSubtitle(subTitle: String) {
        bodyEdit.postDelayed({
            mActivity.supportActionBar!!.subtitle = subTitle
        }, 100)
    }

    fun saveNote(): Boolean {
        if (note.title != titleEdit.text.toString() ||
                note.text != bodyEdit.text.toString() ||
                tagsChanged()) {
            note.title = titleEdit.text.toString()
            note.text = bodyEdit.text.toString()
            note.dirty = true
            note.updatedAt = DateTime.now()
            note.references = tags.map { toReference(it) }
            SApplication.instance.noteStore.putNote(note.uuid, note)
            return true
        }
        return false
    }

    fun toReference(tag: Tag): Reference {
        val ref = Reference()
        ref.contentType = ContentType.Tag.toString()
        ref.uuid = tag.uuid
        return ref
    }

    private fun tagsChanged(): Boolean {
        val oldTagIds = SApplication.instance.noteStore.getTagsForNote(note.uuid).map { it.uuid }.sorted()
        val newTags = tags
        val newTagIds = newTags.map { it.uuid }.sorted()
        return oldTagIds != newTagIds
    }

    override fun onSyncStarted() {
        setSubtitle(getString(R.string.sync_progress_saving))
    }

    override fun onSyncCompleted() {
        setSubtitle(getString(R.string.sync_progress_finished))
    }

    override fun onSyncFailed() {
        setSubtitle(getString(R.string.sync_progress_error))
    }

    override fun onDetach() {
        super.onDetach()
        detachListener.addDrawerToggle()
    }

    interface DetachListener {
        fun addDrawerToggle()
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
