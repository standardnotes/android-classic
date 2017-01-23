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
import org.standardnotes.notes.comms.data.ContentType
import org.standardnotes.notes.comms.data.Reference
import java.util.*

/**
 * Instrumentation test, which will execute on an Android device.

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("org.standardnotes.notes", appContext.packageName)
    }

    @Test
    fun noteStore() {
        val ns = SApplication.instance!!.noteStore
        val n = Note()
        n.text = "text"
        n.title = "title"
        n.uuid = "uuid"
        n.encItemKey = "123"
        val time = DateTime.now()
        n.createdAt = time
        n.updatedAt = time
        ns.putNote(n.uuid, n)
        val n1 = ns.getNote(n.uuid)
        Assert.assertEquals(n.uuid, n1!!.uuid)
        Assert.assertEquals(n.title, n1.title)
        Assert.assertEquals(n.text, n1.text)
        Assert.assertEquals(n.createdAt, n1.createdAt)
    }

    @Test
    fun tagStore() {
        val ns = SApplication.instance!!.noteStore
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
        val ns = SApplication.instance!!.noteStore
        val n = Note()
        n.text = "text"
        n.title = "title"
        n.uuid = UUID.randomUUID().toString()
        n.encItemKey = "123"
        val time = DateTime.now()
        n.createdAt = time
        n.updatedAt = time

        val t = Tag()
        t.title = "title"
        t.uuid = UUID.randomUUID().toString()
        t.encItemKey = "123"
        t.createdAt = time
        t.updatedAt = time
        ns.putTag(t.uuid, t)

        val ref = Reference()
        ref.uuid = t.uuid
        ref.contentType = ContentType.Tag.toString()
        n.references.add(ref)

        ns.putNote(n.uuid, n)

        val n1 = ns.getNote(n.uuid)
        Assert.assertEquals(n.uuid, n1!!.uuid)
        Assert.assertEquals(n.title, n1.title)
        Assert.assertEquals(n.text, n1.text)
        Assert.assertEquals(n.createdAt, n1.createdAt)

        val t1 = ns.getTagsForNote(n.uuid)
        Assert.assertEquals(1, t1.count())
        Assert.assertEquals(ref.uuid, t1[0].uuid)
    }
}
