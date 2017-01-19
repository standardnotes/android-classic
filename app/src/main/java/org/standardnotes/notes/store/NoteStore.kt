package org.standardnotes.notes.store

import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.DirtyNote
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.SyncItems
import java.util.*

/**
 * Created by carl on 17/01/17.
 */

class NoteStore {

    private val list = HashMap<String, Note>()
    private val toSave = HashSet<DirtyNote>()

    var syncToken: String? = null
        get
        private set

    var notesList: List<Note> = ArrayList(0)
        get() = list.values.sortedByDescending { it.original.updatedAt }
        private set


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
        list.put(newNote.original.uuid, newNote)
    }

    @Synchronized fun setDirty(note: DirtyNote) { toSave += note }

    @Synchronized fun popNotesToSave(): Set<DirtyNote> {
        val toSaveCopy = HashSet(toSave)
        toSave.clear()
        return toSaveCopy
    }

    @Synchronized fun notesToSaveCount(): Int {
        return toSave.size
    }



}
