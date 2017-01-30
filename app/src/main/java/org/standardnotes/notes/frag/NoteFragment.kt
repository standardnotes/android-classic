package org.standardnotes.notes.frag

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.standardnotes.notes.R
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.data.Note

import kotlinx.android.synthetic.main.frag_note.*
import org.joda.time.DateTime
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.Tag
import java.util.*

/**
 * Created by carl on 15/01/17.
 */

class NoteFragment : Fragment() {

    var note: Note? = null
    var tags: List<Tag> = Collections.emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteUuid = arguments?.getString("noteId")
        if (noteUuid != null) {
            note = SApplication.instance!!.noteStore.getNote(noteUuid)
            tags = SApplication.instance!!.noteStore.getTagsForNote(noteUuid)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.frag_note, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleEdit.setText(note?.title)
        bodyEdit.setText(note?.text)
        if (tags.count() > 0) {
            tagsRow.visibility = View.VISIBLE
            tags.forEach {
                val tagItem = LayoutInflater.from(activity).inflate(R.layout.item_tag, tagsLayout, false)
                (tagItem.findViewById(R.id.tagText) as TextView).text = it.title
                tagsLayout.addView(tagItem)
            }
        }
        titleLayout.isHintAnimationEnabled = true
    }

    override fun onPause() {
        super.onPause()
        if (activity.isFinishing) {
            // If we are leaving then lets save it locally
            if (note == null) {
                note = newNote()
            }
            val noteV = note!!
            if (noteV.title != titleEdit.text.toString() ||
                    noteV.text != bodyEdit.text.toString()) {
                noteV.title = titleEdit.text.toString()
                noteV.text = bodyEdit.text.toString()
                noteV.dirty = true
                noteV.updatedAt = DateTime.now()
                SApplication.instance!!.noteStore.putNote(noteV.uuid, noteV)
            }
        }
    }


}

fun newNote(): Note {
    // Move to a factory
    val note = Note()
    note.uuid = UUID.randomUUID().toString()
    note.encItemKey = Crypt.generateKey(512)
    note.createdAt = DateTime.now()
    note.updatedAt = note.createdAt
    return note
}
