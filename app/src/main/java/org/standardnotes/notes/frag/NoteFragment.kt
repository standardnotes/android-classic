package org.standardnotes.notes.frag

import android.accounts.Account
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.frag_note.*
import org.joda.time.DateTime
import org.standardnotes.notes.R
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.Tag
import java.util.*

class NoteFragment : Fragment() {

    val SAVE_INTERVAL = 250L
    var saveHandler: Handler = Handler()
    var saveRunnable: Runnable = object : Runnable {
        override fun run() {
            saveNote(note)
            saveHandler.postDelayed(this, SAVE_INTERVAL)
        }
    }

    var note: Note? = null
    var tags: List<Tag> = Collections.emptyList()

    private var account: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        account = SApplication.instance!!.account(null)!! // TODO: Fixme
        if (null == account)
            return
        val noteUuid = arguments?.getString(NoteListFragment.NOTE_FRAGMENT_INTENT)
        if (noteUuid != null) {
            note = SApplication.instance!!.noteStore(account!!).getNote(noteUuid)
            tags = SApplication.instance!!.noteStore(account!!).getTagsForNote(noteUuid)
        }
        startSaveTimer()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.frag_note, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            titleEdit.setText(note?.title)
            bodyEdit.setText(note?.text)
        }
        if (tags.count() > 0) {
            tagsRow.visibility = View.VISIBLE
            tags.forEach {
                val tagItem = LayoutInflater.from(activity).inflate(R.layout.item_tag, tagsLayout, false)
                (tagItem.findViewById(R.id.tagText) as TextView).text = it.title
                tagsLayout.addView(tagItem)
            }
        }
        titleLayout.isHintAnimationEnabled = true

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                startSaveTimer()
            }

            override fun afterTextChanged(s: Editable?) {
                //
            }
        }
        titleEdit.addTextChangedListener(textWatcher)
        bodyEdit.addTextChangedListener(textWatcher)

        titleEdit.setSelection(titleEdit.text.length)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveHandler.removeCallbacks(saveRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (activity.isFinishing) {
            // If we are leaving then lets save it locally
            if (note == null) {
                note = newNote()
            }
            saveNote(note!!)
        }
    }

    fun startSaveTimer() {
        saveHandler.removeCallbacks(saveRunnable)
        saveHandler.postDelayed(saveRunnable, SAVE_INTERVAL)
    }

    fun saveNote(note: Note?) {
        if (note != null && (note.title != titleEdit.text.toString() ||
                note.text != bodyEdit.text.toString())) {
            note.title = titleEdit.text.toString()
            note.text = bodyEdit.text.toString()
            note.dirty = true
            note.updatedAt = DateTime.now()
            SApplication.instance!!.noteStore(account!!).putNote(note.uuid, note)
        }
    }

    fun newNote(): Note {
        // Move to a factory
        val note = Note()
        note.uuid = UUID.randomUUID().toString()
        note.encItemKey = Crypt.generateEncryptedKey(account!!, 512)
        note.createdAt = DateTime.now()
        note.updatedAt = note.createdAt
        return note
    }
}

