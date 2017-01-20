package org.standardnotes.notes.store

import org.standardnotes.notes.comms.Crypt
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

    val notesList: List<Note>
        get() = list.values.sortedByDescending { it.updatedAt }

    val toSave: Collection<Note>
        get() = list.values.filter { it.dirty == true }


    fun putNotes(items: SyncItems) {
        syncToken = items.syncToken
        val allItems = items.retrievedItems + items.savedItems
        for (newItem in allItems) {
            if (newItem.content == null || newItem.deleted) {
                list.remove(newItem.uuid)
                continue
            }
            val old = list.keys.contains(newItem.uuid)
            val newNote = Crypt.decrypt(newItem)
            if (!old) {
                list.put(newItem.uuid, newNote)
            } else {
                merge(newNote)
            }
        }
    }

    private fun merge(newNote: Note) {
        //TODO merge properly
        list.put(newNote.uuid, newNote)
    }

    @Synchronized fun setDirty(note: Note) {
        list[note.uuid] = note
        note.dirty = true
    }

    @Synchronized fun notesToSaveCount(): Int {
        return toSave.size
    }



}
