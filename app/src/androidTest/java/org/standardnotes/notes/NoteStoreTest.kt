package org.standardnotes.notes

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.Tag
import org.standardnotes.notes.store.NoteStore

import org.junit.Assert.*
import org.junit.Before
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.ContentType
import org.standardnotes.notes.comms.data.Reference
import org.standardnotes.notes.frag.newNote
import java.util.*

/**
 * Instrumentation test, which will execute on an Android device.

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class NoteStoreTest {

    @Before
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("Run tests with the emulatortest product flavor, otherwise you will write over your app data",
                "org.standardnotes.notes.buildfortest", appContext.packageName)
    }

    @Test
    fun noteStore() {
        val ns = SApplication.instance.noteStore
        val n = Note()
        n.text = "text"
        n.title = "title"
        n.uuid = "uuid"
        n.encItemKey = "123"
        val time = DateTime.now()
        n.createdAt = time
        n.updatedAt = time
        n.references = ArrayList()
        ns.putNote(n.uuid, n)
        val n1 = ns.getNote(n.uuid)
        assertEquals(n.uuid, n1!!.uuid)
        Assert.assertEquals(n.title, n1.title)
        Assert.assertEquals(n.text, n1.text)
        Assert.assertEquals(n.createdAt, n1.createdAt)

        n.title = UUID.randomUUID().toString()
        ns.putNote(n.uuid, n)
        val changedNote = ns.getNote(n.uuid)
        Assert.assertEquals(changedNote!!.title, n.title)

        val oldTitle = changedNote.title
        changedNote.title = UUID.randomUUID().toString()
        changedNote.updatedAt = DateTime.now()
        ns.mergeNote(changedNote.uuid, changedNote)
        val mergedNote = ns.getNote(changedNote.uuid)
        Assert.assertEquals(oldTitle, mergedNote!!.title)
        Assert.assertEquals(n.text, mergedNote.text)
        Assert.assertEquals(changedNote.updatedAt, mergedNote.updatedAt)

    }

    @Test
    fun tagStore() {
        val ns = SApplication.instance.noteStore
        val t = Tag()
        t.title = "title"
        t.uuid = "uuid2"
        t.encItemKey = "123"
        val time = DateTime.now()
        t.createdAt = time
        t.updatedAt = time
        ns.putTag(t.uuid, t)
        val n1 = ns.getTag(t.uuid)
        Assert.assertEquals(t.uuid, n1!!.uuid)
        Assert.assertEquals(t.title, n1.title)
        Assert.assertEquals(t.createdAt, n1.createdAt)
    }

    @Test
    fun noteStoreWithTag() {
        val ns = SApplication.instance.noteStore
        val n = Note()
        n.text = UUID.randomUUID().toString()
        n.title = UUID.randomUUID().toString()
        n.uuid = UUID.randomUUID().toString()
        n.encItemKey = "123"
        val time = DateTime.now()
        n.createdAt = time
        n.updatedAt = time

        val t = Tag()
        t.title = UUID.randomUUID().toString()
        t.uuid = UUID.randomUUID().toString()
        t.encItemKey = "123"
        t.createdAt = time
        t.updatedAt = time
        ns.putTag(t.uuid, t)

        val ref = Reference()
        ref.uuid = t.uuid
        ref.contentType = ContentType.Tag.toString()
        n.references = ArrayList()
        n.references.add(ref)

        ns.putNote(n.uuid, n)

        val n1 = ns.getNote(n.uuid)
        Assert.assertEquals(n.uuid, n1!!.uuid)
        Assert.assertEquals(n.title, n1.title)
        Assert.assertEquals(n.text, n1.text)
        Assert.assertEquals(n.createdAt, n1.createdAt)

        val nTags = ns.getTagsForNote(n.uuid)
        Assert.assertEquals(1, nTags.count())
        Assert.assertEquals(ref.uuid, nTags[0].uuid)
        Assert.assertEquals(n.references[0].uuid, nTags[0].uuid)

        Assert.assertEquals(1, ns.getNotesForTag(t.uuid).count())
        Assert.assertEquals(n1.uuid, ns.getNotesForTag(t.uuid)[0].uuid)

        val t2 = Tag()
        t2.title = UUID.randomUUID().toString()
        t2.uuid = UUID.randomUUID().toString()
        t2.encItemKey = "123"
        t2.createdAt = time
        t2.updatedAt = time
        ns.putTag(t2.uuid, t2)

        val ref2 = Reference()
        ref2.uuid = t2.uuid
        ref2.contentType = ContentType.Tag.toString()
        n.references.add(ref2)
        ns.putNote(n.uuid, n)
        Assert.assertEquals(1, ns.getNotesForTag(t2.uuid).count())
        Assert.assertEquals(2, ns.getTagsForNote(n.uuid).count())

        n.references.clear()
        ns.putNote(n.uuid, n)

        Assert.assertEquals(0, ns.getNotesForTag(t.uuid).count())
        Assert.assertEquals(0, ns.getTagsForNote(n.uuid).count())
    }

    @Test
    fun encrypt001() {
        val mk = "96fbfbace17d0d268cc5a57900fe785e50a40cf7ae2d23a3dcdd2f28d5fd09d8"
        SApplication.instance.valueStore.setTokenAndMasterKey("", mk, null)

        val n = newNote()
        n.text = UUID.randomUUID().toString()
        n.title = UUID.randomUUID().toString()

        val cn = Crypt.encrypt(n, "001")
        val n2 = Crypt.decryptNote(cn)
        Assert.assertEquals(n.uuid, n2.uuid)
        Assert.assertEquals(n.title, n2.title)
        Assert.assertEquals(n.text, n2.text)
        Assert.assertEquals(n.createdAt, n2.createdAt)
        Assert.assertEquals(n.updatedAt, n2.updatedAt)

    }

    @Test
    fun encrypt002() {
        val mk = "96fbfbace17d0d268cc5a57900fe785e50a40cf7ae2d23a3dcdd2f28d5fd09d8"
        val ak = "f6fsfbacd17d0d268cc5a57900fe785e50a40cf7ae2d23a3dcdd2f28d5fd09d2"
        SApplication.instance.valueStore.setTokenAndMasterKey("", mk, ak)

        val n = Note()
        n.uuid = UUID.randomUUID().toString()
        n.encItemKey = Crypt.generateEncryptedKey(512, "002", n.uuid)
        n.createdAt = DateTime.now()
        n.updatedAt = n.createdAt
        n.text = UUID.randomUUID().toString()
        n.title = UUID.randomUUID().toString()

        val cn = Crypt.encrypt(n, "002")
        val n2 = Crypt.decryptNote(cn)
        Assert.assertEquals(n.uuid, n2.uuid)
        Assert.assertEquals(n.title, n2.title)
        Assert.assertEquals(n.text, n2.text)
        Assert.assertEquals(n.createdAt, n2.createdAt)
        Assert.assertEquals(n.updatedAt, n2.updatedAt)

    }

    fun createString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        val sb = StringBuilder(length)
        val random = Random()
        for (i in 0..length) {
            val c = chars[random.nextInt(chars.size)]
            sb.append(c)
        }
        return sb.toString()
    }

    @Test
    fun bigStoreThreadedRead() {
        val ns = SApplication.instance.noteStore
        val n1 = createLargeNote()
        for (i in 1..10) {
            createLargeNote()
            createLargeNote()
            createLargeNote()
            Thread(Runnable { ns.getAllNotes() }).start()
        }
        val n2 = createLargeNote()
        val n3 = createLargeNote()
        val n4 = createLargeNote()
        val n1r = ns.getNote(n1.uuid)
        Assert.assertEquals(n1r?.uuid, n1.uuid)
    }

    fun createLargeNote(): Note {
        val ns = SApplication.instance.noteStore
        val n = Note()
        n.text = createString(50000)
        n.encItemKey = "123"
        n.uuid = UUID.randomUUID().toString()
        n.title = n.uuid
        val time = DateTime.now()
        n.createdAt = time
        n.updatedAt = time
        n.references = ArrayList()
        ns.putNote(n.uuid, n)
        return n
    }

}
