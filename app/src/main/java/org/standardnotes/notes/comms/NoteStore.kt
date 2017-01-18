package org.standardnotes.notes.comms

import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.SyncItems
import java.util.*

/**
 * Created by carl on 17/01/17.
 */

class NoteStore {

    private val list = HashMap<String, Note>()

    var syncToken: String? = null
        get
        private set

    var notesList: List<Note> = ArrayList(0)
        get() = list.values.sortedBy { it.original.updatedAt }
        private set



    fun putNotes(items: SyncItems) {
        syncToken = items.syncToken
        for (newItem in items.retrievedItems) {
            if (newItem.content == null || newItem.deleted) {
                continue // avoid the blank items
            }
            val old = list.keys.contains(newItem.encItemKey)
            val newNote = Crypt.decrypt(newItem)
            if (!old) {
                list.put(newItem.encItemKey, newNote)
            } else {
                merge(newNote)
            }
        }
    }

    private fun merge(newNote: Note) {
        //TODO merge properly
        list.put(newNote.original.encItemKey, newNote)
    }


}
