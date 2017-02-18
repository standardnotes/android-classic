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

val CURRENT_DB_VERSION: Int = 1

// TODO move this to async access
class NoteStore : SQLiteOpenHelper(SApplication.instance, "note", null, CURRENT_DB_VERSION) {


//    private val noteList = HashMap<String, Note>()
//    private val tagList = HashMap<String, Tag>()


    val notesList: List<Note>
        get() = getAllNotes()

    val toSave: Collection<EncryptableItem> // TODO inefficient, convert to query
        get() = getAllTags().filter { it.dirty == true } + getAllNotes().filter { it.dirty == true }

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
    // also contains KEY_UUID

    private val TABLE_NOTE_TAG = "NOTE_TAG"
    private val KEY_NOTE_UUID = "NOTE_ID"
    private val KEY_TAG_UUID = "TAG_ID"


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NOTE ($KEY_UUID TEXT, $KEY_TITLE TEXT, $KEY_TEXT TEXT)")
        db.execSQL("CREATE TABLE $TABLE_TAG ($KEY_UUID TEXT, $KEY_TITLE TEXT)")
        db.execSQL("CREATE TABLE $TABLE_ENCRYPTABLE ($KEY_UUID TEXT, $KEY_CREATED_AT INTEGER, $KEY_UPDATED_AT INTEGER, $KEY_ENC_ITEM_KEY TEXT, $KEY_PRESENTATION_NAME TEXT, $KEY_DELETED BOOLEAN, $KEY_DIRTY BOOLEAN)")
        db.execSQL("CREATE TABLE $TABLE_NOTE_TAG ($KEY_NOTE_UUID TEXT, $KEY_TAG_UUID TEXT)")

        db.execSQL("CREATE INDEX IDX_NOTE ON $TABLE_NOTE ($KEY_UUID)")
        db.execSQL("CREATE INDEX IDX_TAG ON $TABLE_TAG ($KEY_UUID)")
        db.execSQL("CREATE INDEX IDX_ENCRTYPTABLE ON $TABLE_ENCRYPTABLE ($KEY_UUID)")
        db.execSQL("CREATE INDEX IDX_NOTE_TAG_TAG ON $TABLE_NOTE_TAG ($KEY_TAG_UUID)")
        db.execSQL("CREATE INDEX IDX_NOTE_TAG_NOTE ON $TABLE_NOTE_TAG ($KEY_NOTE_UUID)")
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
                putNoteTagRelationships(db, uuid, item.references.filter { it.contentType == ContentType.Tag.toString() }.map { it.uuid }.toSet())
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
                //putNoteTagRelationships(db, uuid, item.references.filter { it.contentType == ContentType.Note.toString() }.map { it.uuid })
            }
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }

    @Synchronized fun putNoteTagRelationships(db: SQLiteDatabase, noteUuid: String, tagUuid: Set<String>) {

        fun makeTagsDirty(db: SQLiteDatabase) {
            db.execSQL("UPDATE $TABLE_ENCRYPTABLE " +
                    " SET $KEY_DIRTY=1" +
                    " WHERE $KEY_UUID IN " +
                    "(SELECT $KEY_TAG_UUID FROM $TABLE_NOTE_TAG" +
                    " WHERE $KEY_NOTE_UUID=?)", arrayOf(noteUuid))
        }
        makeTagsDirty(db)
        db.delete(TABLE_NOTE_TAG, "$KEY_NOTE_UUID=?", arrayOf(noteUuid))
        tagUuid.forEach {
            db.insert(TABLE_NOTE_TAG, null, ContentValues().apply {
                put(KEY_TAG_UUID, it)
                put(KEY_NOTE_UUID, noteUuid)
            })
        }
        makeTagsDirty(db)

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

    // TODO: should we display notes that are deleted but that deletion is not synced with the server?
    fun getAllNotes(uuid: String?): List<Note> {
        val db = readableDatabase
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
            note.references = getReferences(note.uuid, ContentType.Tag)
            items.add(note)
        }
        items.sortByDescending { it.updatedAt }
        return items
    }

    fun getReferences(uuid: String, type: ContentType): List<Reference> {
        val db = readableDatabase
        val cur = if (type == ContentType.Tag)
            db.query(TABLE_NOTE_TAG, arrayOf(KEY_TAG_UUID), "$KEY_NOTE_UUID=?", arrayOf(uuid), null, null, null)
        else
            db.query(TABLE_NOTE_TAG, arrayOf(KEY_NOTE_UUID), "$KEY_TAG_UUID=?", arrayOf(uuid), null, null, null)
        val refs = ArrayList<Reference>(cur.count)
        while (cur.moveToNext()) {
            val ref = Reference()
            ref.contentType = type.toString()
            ref.uuid = cur.getString(0)
            refs.add(ref)
        }
        return refs
    }


    fun getAllNotes(): List<Note> {
        return getAllNotes(null)
    }

    fun getNote(uuid: String): Note? {
        return getAllNotes(uuid).getOrNull(0)
    }

    fun getTagsForNote(noteUuid: String): List<Tag> {
        val db = readableDatabase
        val cur = db.rawQuery("SELECT t.*, e.* FROM $TABLE_TAG t" +
                " INNER JOIN $TABLE_ENCRYPTABLE e ON t.$KEY_UUID=e.$KEY_UUID" +
                " INNER JOIN $TABLE_NOTE_TAG nt ON nt.$KEY_TAG_UUID=t.$KEY_UUID" +
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


    fun getNotesForTag(tagUuid: String): List<Note> {
        val db = readableDatabase
        val cur = db.rawQuery("SELECT n.*, e.* FROM $TABLE_NOTE n" +
                " INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID" +
                " INNER JOIN $TABLE_NOTE_TAG nt ON nt.$KEY_NOTE_UUID=n.$KEY_UUID" +
                " WHERE nt.$KEY_TAG_UUID=?", arrayOf(tagUuid))
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

        return items
    }

    fun getAllTags(forNoteUuid: String?): List<Tag> {
        val db = readableDatabase
        val cur = if (forNoteUuid == null)
            db.rawQuery("SELECT * FROM $TABLE_TAG n INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID", null)
        else
            db.rawQuery("SELECT * FROM $TABLE_TAG n INNER JOIN $TABLE_ENCRYPTABLE e ON n.$KEY_UUID=e.$KEY_UUID WHERE n.$KEY_UUID=?", arrayOf(forNoteUuid))
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
            tag.references = getReferences(tag.uuid, ContentType.Note)
            items.add(tag)
        }
        return items
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

    @Synchronized fun putItems(items: SyncItems) {
        SApplication.instance.valueStore.syncToken = items.syncToken.trim()
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

    fun mergeNote(uuid: String, newNote: Note?) {
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

//    @Synchronized fun setTagsOnNote(noteUuid: String, newTagIds: Set<String>) {
//        val db = writableDatabase
//        db.use {
//            db.beginTransaction()
//            putNoteTagRelationships(db, noteUuid, newTagIds)
//            setDirty(db, newTagIds, true)
//            db.setTransactionSuccessful()
//            db.endTransaction()
//        }
//    }

    private fun copyMetadataIntoOld(newNote: EncryptableItem, oldNote: EncryptableItem) {
        oldNote.presentationName = newNote.presentationName
        oldNote.createdAt = newNote.createdAt
        oldNote.updatedAt = newNote.updatedAt
        oldNote.dirty = false // We're saving so we can clear the dirty flag
    }

    fun mergeTag(uuid: String, newTag: Tag?) {
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

    private fun setDirty(db: SQLiteDatabase, uuids: Collection<String>, dirty: Boolean) {
        uuids.forEach {
            db.update(TABLE_ENCRYPTABLE,
                    ContentValues().apply {
                        put(KEY_DIRTY, dirty)
                    },
                    "$KEY_UUID=?", arrayOf(it))
        }
    }

    @Synchronized fun notesToSaveCount(): Int {
        return toSave.size
    }

    @Synchronized fun deleteAll() {
        SApplication.instance.valueStore.syncToken = null
        writableDatabase.use {
            writableDatabase.delete(TABLE_NOTE, null, null)
            writableDatabase.delete(TABLE_TAG, null, null)
            writableDatabase.delete(TABLE_ENCRYPTABLE, null, null)
        }
        close()
        SApplication.instance.deleteDatabase("note")
    }
}

private fun EncryptedItem.isValid(): Boolean {
    return (contentTypeFromString(contentType) != null && // Not a type this client understands
            (content != null || deleted) &&
            uuid != null)
}

