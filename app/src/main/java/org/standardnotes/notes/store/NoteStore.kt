package org.standardnotes.notes.store

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.*
import java.util.*

/**
 * Created by carl on 17/01/17.
 */


val CURRENT_DB_VERSION: Int = 1

// TODO move this to async access
class NoteStore : SQLiteOpenHelper(SApplication.instance, "note", null, CURRENT_DB_VERSION) {


//    private val noteList = HashMap<String, Note>()
//    private val tagList = HashMap<String, Tag>()


    val notesList: List<Note>
        get() = getAllNotes()

    val toSave: Collection<Note> // TODO inefficient, convert to query
        get() = getAllNotes().filter { it.dirty == true }

    private val TABLE_NOTE = "NOTE"
    private val KEY_UUID = "ID"
    private val KEY_TITLE = "TITLE"
    private val KEY_TEXT = "TEXT"
    private val KEY_DIRTY = "DIRTY"

    private val TABLE_TAG = "TAG"
    // also contains KEY_TITLE, KEY_UUID

    private val TABLE_ENCRYPTABLE = "ENCRYPTABLE"
    private val KEY_CREATED_AT = "CREATED"
    private val KEY_UPDATED_AT = "UPDATED"
    private val KEY_ENC_ITEM_KEY = "ENC_ITEM_KEY"
    private val KEY_PRESENTATION_NAME = "PRESENTATION_NAME"
    private val KEY_DELETED = "DELETED"
    private val KEY_REFERENCES = "REFS"
    // also contains KEY_UUID


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NOTE ($KEY_UUID TEXT, $KEY_TITLE TEXT, $KEY_TEXT TEXT)")
        db.execSQL("CREATE TABLE $TABLE_TAG ($KEY_UUID TEXT, $KEY_TITLE TEXT)")
        db.execSQL("CREATE TABLE $TABLE_ENCRYPTABLE ($KEY_UUID TEXT, $KEY_CREATED_AT INTEGER, $KEY_UPDATED_AT INTEGER, $KEY_ENC_ITEM_KEY TEXT, $KEY_PRESENTATION_NAME TEXT, $KEY_DELETED BOOLEAN, $KEY_DIRTY BOOLEAN, $KEY_REFERENCES TEXT)")

        db.execSQL("CREATE INDEX IDX_NOTE ON $TABLE_NOTE ($KEY_UUID)")
        db.execSQL("CREATE INDEX IDX_TAG ON $TABLE_TAG ($KEY_UUID)")
        db.execSQL("CREATE INDEX IDX_ENCRTYPTABLE ON $TABLE_ENCRYPTABLE ($KEY_UUID)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    @Synchronized fun putNote(uuid: String, item: Note?) {
        val db = writableDatabase

        // TODO gotcha for when adding pre-lollipop support http://stackoverflow.com/questions/39430179/kotlin-closable-and-sqlitedatabase-on-android
        db.use {
            db.beginTransaction()
            db.delete(TABLE_NOTE, "$KEY_UUID=?", arrayOf(uuid))
            db.delete(TABLE_ENCRYPTABLE, "$KEY_UUID=?", arrayOf(uuid))
            if (item != null && !item.deleted) {
                db.insert(TABLE_NOTE, null, ContentValues().apply {
                    put(KEY_UUID, item.uuid)
                    put(KEY_TITLE, item.title)
                    put(KEY_TEXT, item.text)
                })
                insertEncryptable(db, item)
            }
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }

    @Synchronized fun putTag(uuid: String, item: Tag?) {
        val db = writableDatabase
        db.use {
            db.beginTransaction()
            db.delete(TABLE_TAG, "$KEY_UUID=?", arrayOf(uuid))
            db.delete(TABLE_ENCRYPTABLE, "$KEY_UUID=?", arrayOf(uuid))
            if (item != null && !item.deleted) {
                db.insert(TABLE_TAG, null, ContentValues().apply {
                    put(KEY_UUID, item.uuid)
                    put(KEY_TITLE, item.title)
                })
                insertEncryptable(db, item)
            }
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }

    private fun insertEncryptable(db: SQLiteDatabase, item: EncryptableItem) {
        db.insert(TABLE_ENCRYPTABLE, null, ContentValues().apply {
            put(KEY_UUID, item.uuid)
            put(KEY_CREATED_AT, item.createdAt.millis)
            put(KEY_UPDATED_AT, item.updatedAt.millis)
            put(KEY_ENC_ITEM_KEY, item.encItemKey)
            put(KEY_PRESENTATION_NAME, item.presentationName)
            put(KEY_DELETED, item.deleted)
            put(KEY_DIRTY, item.dirty)
            put(KEY_REFERENCES, SApplication.instance!!.gson.toJson(item.references))
        })
    }

    // TODO: should we display notes that are deleted but that deletion is not synced with the server?
    fun getAllNotes(uuid: String?): List<Note> {
        val db = readableDatabase
        db.use {
            val cur = if (uuid == null)
                db.rawQuery("SELECT * FROM $TABLE_NOTE n INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID", null)
            else
                db.rawQuery("SELECT * FROM $TABLE_NOTE n INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID WHERE n.$KEY_UUID=?", arrayOf(uuid))
            val items = ArrayList<Note>(cur.count)
            while (cur.moveToNext()) {
                val note = Note()
                note.deleted = cur.getInt(cur.getColumnIndex(KEY_DELETED)) == 1
                note.uuid = cur.getString(cur.getColumnIndex(KEY_UUID))
                note.title = cur.getString(cur.getColumnIndex(KEY_TITLE))
                note.text = cur.getString(cur.getColumnIndex(KEY_TEXT))
                note.dirty = cur.getInt(cur.getColumnIndex(KEY_DIRTY)) == 1
                note.createdAt = DateTime(cur.getLong(cur.getColumnIndex(KEY_CREATED_AT)))
                note.updatedAt = DateTime(cur.getLong(cur.getColumnIndex(KEY_UPDATED_AT)))
                note.encItemKey = cur.getString(cur.getColumnIndex(KEY_ENC_ITEM_KEY))
                note.presentationName = cur.getString(cur.getColumnIndex(KEY_PRESENTATION_NAME))
                val listType = object : TypeToken<List<Reference>>() {}.type
                note.references = SApplication.instance!!.gson.fromJson(cur.getString(cur.getColumnIndex(KEY_REFERENCES)), listType)
                items.add(note)
            }
            items.sortByDescending { it.updatedAt }
            return items
        }
    }

    fun getAllNotes(): List<Note> {
        return getAllNotes(null)
    }

    fun getNote(uuid: String): Note? {
        return getAllNotes(uuid).getOrNull(0)
    }

    fun getTagsForNote(noteUuid: String): List<Tag> {
        val note = getNote(noteUuid)
        val db = readableDatabase
        db.use {
            val items = ArrayList<Tag>(note?.references?.size ?: 0)
            note?.references?.filter { it.contentType == ContentType.Tag.toString() }
                    ?.forEach {
                        val cur = db.rawQuery("SELECT * FROM $TABLE_TAG n INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID" +
                                " WHERE n.$KEY_UUID=?", arrayOf(it.uuid))
                        while (cur.moveToNext()) {
                            val tag = Tag()
                            tag.uuid = cur.getString(cur.getColumnIndex(KEY_UUID))
                            tag.title = cur.getString(cur.getColumnIndex(KEY_TITLE))
                            tag.dirty = cur.getInt(cur.getColumnIndex(KEY_DIRTY)) == 1
                            tag.createdAt = DateTime(cur.getLong(cur.getColumnIndex(KEY_CREATED_AT)))
                            tag.updatedAt = DateTime(cur.getLong(cur.getColumnIndex(KEY_UPDATED_AT)))
                            tag.encItemKey = cur.getString(cur.getColumnIndex(KEY_ENC_ITEM_KEY))
                            tag.presentationName = cur.getString(cur.getColumnIndex(KEY_PRESENTATION_NAME))
                            tag.deleted = cur.getInt(cur.getColumnIndex(KEY_DELETED)) == 1
                            val listType = object : TypeToken<List<Reference>>() {}.type
                            tag.references = SApplication.instance!!.gson.fromJson(cur.getString(cur.getColumnIndex(KEY_REFERENCES)), listType)
                            items.add(tag)
                        }
                    }
            return items
        }
    }

    fun getAllTags(uuid: String?): List<Tag> {
        val db = readableDatabase
        db.use {
            val cur = if (uuid == null)
                db.rawQuery("SELECT * FROM $TABLE_TAG n INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID", null)
            else
                db.rawQuery("SELECT * FROM $TABLE_TAG n INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID WHERE n.$KEY_UUID=?", arrayOf(uuid))
            val items = ArrayList<Tag>(cur.count)
            while (cur.moveToNext()) {
                val tag = Tag()
                tag.uuid = cur.getString(cur.getColumnIndex(KEY_UUID))
                tag.title = cur.getString(cur.getColumnIndex(KEY_TITLE))
                tag.dirty = cur.getInt(cur.getColumnIndex(KEY_DIRTY)) == 1
                tag.createdAt = DateTime(cur.getLong(cur.getColumnIndex(KEY_CREATED_AT)))
                tag.updatedAt = DateTime(cur.getLong(cur.getColumnIndex(KEY_UPDATED_AT)))
                tag.encItemKey = cur.getString(cur.getColumnIndex(KEY_ENC_ITEM_KEY))
                tag.presentationName = cur.getString(cur.getColumnIndex(KEY_PRESENTATION_NAME))
                tag.deleted = cur.getInt(cur.getColumnIndex(KEY_DELETED)) == 1
                items.add(tag)
            }
            return items
        }
    }

    fun getTag(uuid: String): Tag? {
        return getAllTags(uuid).getOrNull(0)
    }

    fun getAllTags(): List<Tag> {
        return getAllTags(null)
    }

    fun deleteItem(uuid: String) {
        val db = writableDatabase
        db.use {
            db.beginTransaction()
            db.update(TABLE_ENCRYPTABLE,
                    ContentValues().apply {
                        put(KEY_DELETED, true)
                        put(KEY_DIRTY, true)
                    },
                    "$KEY_UUID=?", arrayOf(uuid))
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }

    fun putItems(items: SyncItems) {
        SApplication.instance!!.valueStore.syncToken = items.syncToken
        items.retrievedItems.forEach { putItem(it, false) }
        items.savedItems.forEach { putItem(it, true) }
    }

    private fun putItem(item: EncryptedItem, mergeWithOld: Boolean) {
        if (!item.isValid()) return

        val type = contentTypeFromString(item.contentType)
        if (type == ContentType.Note) {
            var newNote: Note?
            if (item.deleted) {
                newNote = null
            } else {
                newNote = Crypt.decryptNote(item)
            }

            if (mergeWithOld) {
                mergeNote(item.uuid, newNote)
            } else {
                putNote(item.uuid, newNote)
            }
        } else if (type == ContentType.Tag) {
            var newTag: Tag?
            if (item.deleted) {
                newTag = null
            } else {
                newTag = Crypt.decryptTag(item)
            }

            if (mergeWithOld) {
                mergeTag(item.uuid, newTag)
            } else {
                putTag(item.uuid, newTag)
            }
        }
    }

    private fun mergeNote(uuid: String, newNote: Note?) {
        // TODO if content_type changes, we have a problem
        val old = getNote(uuid)
        if (old != null && newNote != null) {
            assert(!newNote.deleted)
            copyMetadataIntoOld(newNote, old)
            putNote(uuid, old)
        } else {
            putNote(uuid, newNote)
        }
    }

    private fun copyMetadataIntoOld(newNote: EncryptableItem, oldNote: EncryptableItem) {
        oldNote.presentationName = newNote.presentationName
        oldNote.createdAt = newNote.createdAt
        oldNote.references = newNote.references
        oldNote.updatedAt = newNote.updatedAt
        oldNote.dirty = false // We're saving so we can clear the dirty flag
    }

    private fun mergeTag(uuid: String, newTag: Tag?) {
        // TODO if content_type changes, we have a problem
        val old = getTag(uuid)
        putTag(uuid, newTag)

        if (old != null && newTag != null) {
            assert(!newTag.deleted)
            copyMetadataIntoOld(newTag, old)
            putTag(uuid, old)
        } else {
            putTag(uuid, newTag)
        }
    }

    @Synchronized fun setDirty(uuids: Array<String>, dirty: Boolean) {
        val db = writableDatabase
        db.use {
            db.beginTransaction()
            db.update(TABLE_ENCRYPTABLE,
                    ContentValues().apply {
                        put(KEY_DIRTY, dirty)
                    },
                    "$KEY_UUID=?", uuids)
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }

    @Synchronized fun notesToSaveCount(): Int {
        return toSave.size
    }

    @Synchronized fun deleteAll() {
        SApplication.instance!!.valueStore.syncToken = null
        writableDatabase.use {
            writableDatabase.delete(TABLE_NOTE, null, null)
            writableDatabase.delete(TABLE_TAG, null, null)
            writableDatabase.delete(TABLE_ENCRYPTABLE, null, null)
        }
        close()
        SApplication.instance!!.deleteDatabase("note")
    }


}

private fun EncryptedItem.isValid(): Boolean {
    return (contentTypeFromString(contentType) != null && // Not a type this client understands
            content != null &&
            uuid != null)
}

