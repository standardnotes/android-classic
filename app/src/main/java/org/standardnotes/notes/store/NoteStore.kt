package org.standardnotes.notes.store

import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.*
import java.util.*

/**
 * Created by carl on 17/01/17.
 */

// TODO back by persistent storage
class NoteStore {

    private val noteList = HashMap<String, Note>()
    private val tagList = HashMap<String, Tag>()

    var syncToken: String? = null
        get
        private set

    val notesList: List<Note>
        get() = noteList.values.sortedByDescending { it.updatedAt }

    val toSave: Collection<Note>
        get() = noteList.values.filter { it.dirty == true }


    fun putItems(items: SyncItems) {
        syncToken = items.syncToken
        val allItems = items.retrievedItems + items.savedItems
        for (newItem in allItems) {
            if (newItem.content == null  // HACK until I have delete to clear up accidentally created blank items
                    || newItem.deleted) {
                noteList.remove(newItem.uuid)
                continue
            }
            val type = contentTypeFromString(newItem.contentType)
            if (type == ContentType.Note) {
                val old = noteList.keys.contains(newItem.uuid)
                val newNote = Crypt.decryptNote(newItem)
                if (!old) {
                    noteList.put(newItem.uuid, newNote)
                } else {
                    mergeNote(newNote)
                }
            } else if (type == ContentType.Tag) {
                val old = tagList.keys.contains(newItem.uuid)
                val newTag = Crypt.decryptTag(newItem)
                if (!old) {
                    tagList.put(newItem.uuid, newTag)
                } else {
                    mergeTag(newTag)
                }
            }
        }
    }

    private fun mergeNote(newNote: Note) {
        //TODO merge properly
        noteList.put(newNote.uuid, newNote)
    }

    private fun mergeTag(newTag: Tag) {
        //TODO merge properly
        tagList.put(newTag.uuid, newTag)
    }

    @Synchronized fun setDirty(note: Note) {
        noteList[note.uuid] = note
        note.dirty = true
    }

    @Synchronized fun notesToSaveCount(): Int {
        return toSave.size
    }



}
