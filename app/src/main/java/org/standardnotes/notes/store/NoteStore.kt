package org.standardnotes.notes.store

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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

    var syncToken: String? = null // TODO store this permanently
        get
        private set

    val notesList: List<Note>
        get() = getAllNotes()

    val toSave: Collection<Note>
        get() = getAllNotes().filter { it.dirty == true }

    private val TABLE_NOTE = "NOTE"
    private val KEY_UUID = "ID"
    private val KEY_TITLE = "TITLE"
    private val KEY_TEXT = "TEXT"
    private val KEY_DIRTY = "DIRTY"

    private val TABLE_NOTE_TAG = "NOTE_TAG"
    private val KEY_NOTE_UUID = "NOTE_ID"
    private val KEY_TAG_UUID = "TAG_ID"

    private val TABLE_TAG = "TAG"
    // also contains KEY_TITLE, KEY_UUID

    private val TABLE_ENCRYPTABLE = "ENCRYPTABLE"
    private val KEY_CREATED_AT = "CREATED"
    private val KEY_UPDATED_AT = "UPDATED"
    private val KEY_ENC_ITEM_KEY = "ENC_ITEM_KEY"
    private val KEY_PRESENTATION_NAME = "PRESENTATION_NAME"
    private val KEY_DELETED = "DELETED"
    // also contains KEY_UUID


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NOTE ($KEY_UUID TEXT, $KEY_TITLE TEXT, $KEY_TEXT TEXT)")
        db.execSQL("CREATE TABLE $TABLE_TAG ($KEY_UUID TEXT, $KEY_TITLE TEXT)")
        db.execSQL("CREATE TABLE $TABLE_NOTE_TAG ($KEY_NOTE_UUID TEXT, $KEY_TAG_UUID TEXT)")
        db.execSQL("CREATE TABLE $TABLE_ENCRYPTABLE ($KEY_UUID TEXT, $KEY_CREATED_AT INTEGER, $KEY_UPDATED_AT INTEGER, $KEY_ENC_ITEM_KEY TEXT, $KEY_PRESENTATION_NAME TEXT, $KEY_DELETED BOOLEAN, $KEY_DIRTY BOOLEAN)")

        db.execSQL("CREATE INDEX IDX_NOTE ON $TABLE_NOTE ($KEY_UUID)")
        db.execSQL("CREATE INDEX IDX_NOTE_TAG ON $TABLE_NOTE_TAG ($KEY_NOTE_UUID, $KEY_TAG_UUID)")
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
            db.delete(TABLE_NOTE_TAG, "$KEY_NOTE_UUID=?", arrayOf(uuid))
            if (item != null && !item.deleted) {
                db.insert(TABLE_NOTE, null, ContentValues().apply {
                    put(KEY_UUID, item.uuid)
                    put(KEY_TITLE, item.title)
                    put(KEY_TEXT, item.text)
                })
                insertEncryptable(db, item)
                item.references
                        .filter { ContentType.valueOf(it.contentType) == ContentType.Tag }
                        .forEach {
                            db.insert(TABLE_NOTE_TAG, null, ContentValues().apply {
                                put(KEY_NOTE_UUID, item.uuid)
                                put(KEY_TAG_UUID, it.uuid)
                            })
                        }
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
        })
    }

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
                note.uuid = cur.getString(cur.getColumnIndex(KEY_UUID))
                note.title = cur.getString(cur.getColumnIndex(KEY_TITLE))
                note.text = cur.getString(cur.getColumnIndex(KEY_TEXT))
                note.dirty = cur.getInt(cur.getColumnIndex(KEY_DIRTY)) == 1
                note.createdAt = DateTime(cur.getLong(cur.getColumnIndex(KEY_CREATED_AT)))
                note.updatedAt = DateTime(cur.getLong(cur.getColumnIndex(KEY_UPDATED_AT)))
                note.encItemKey = cur.getString(cur.getColumnIndex(KEY_ENC_ITEM_KEY))
                note.presentationName = cur.getString(cur.getColumnIndex(KEY_PRESENTATION_NAME))
                note.deleted = cur.getInt(cur.getColumnIndex(KEY_DELETED)) == 1
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
        val db = readableDatabase
        db.use {
            val cur = db.rawQuery("SELECT * FROM $TABLE_TAG n INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID" +
                    " INNER JOIN $TABLE_NOTE_TAG nt ON nt.$KEY_TAG_UUID=n.$KEY_UUID" +
                    " WHERE nt.$KEY_NOTE_UUID=?", arrayOf(noteUuid))
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

    fun putItems(items: SyncItems) {
        syncToken = items.syncToken
        val allItems = items.retrievedItems + items.savedItems
        for (newItem in allItems) {

            val type = contentTypeFromString(newItem.contentType)
            if (type == ContentType.Note) {
                var newNote: Note?
                if (//newItem.content == null || // HACK until I have delete to clear up accidentally created blank items
                    newItem.deleted) {
                    newNote = null
                } else {
                    newNote = Crypt.decryptNote(newItem)
                }

                mergeNote(newItem.uuid, newNote)
            } else if (type == ContentType.Tag) {
                var newTag: Tag?
                if (//newItem.content == null || // HACK until I have delete to clear up accidentally created blank items
                    newItem.deleted) {
                    newTag = null
                } else {
                    newTag = Crypt.decryptTag(newItem)
                }

                mergeTag(newItem.uuid, newTag)
            }
        }
    }

    private fun mergeNote(uuid: String, newNote: Note?) {
        val old = getNote(uuid)
        //TODO merge properly
        putNote(uuid, newNote)
    }

    private fun mergeTag(uuid: String, newTag: Tag?) {
        //TODO merge properly
        val old = getTag(uuid)
        //TODO merge properly
        putTag(uuid, newTag)
    }

    @Synchronized fun setDirty(note: Note) {
        note.dirty = true
        putNote(note.uuid, note)
    }

    @Synchronized fun notesToSaveCount(): Int {
        return toSave.size
    }

    @Synchronized fun deleteAll() {
        syncToken = null
        writableDatabase.use {
            writableDatabase.delete(TABLE_NOTE, null, null)
            writableDatabase.delete(TABLE_TAG, null, null)
            writableDatabase.delete(TABLE_NOTE_TAG, null, null)
            writableDatabase.delete(TABLE_ENCRYPTABLE, null, null)
        }
        close()
        SApplication.instance!!.deleteDatabase("note")
    }


}

