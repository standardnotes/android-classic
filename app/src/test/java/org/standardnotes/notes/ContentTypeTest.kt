package org.standardnotes.notes

import org.junit.Assert.assertEquals
import org.junit.Test
import org.standardnotes.notes.comms.data.ContentType
import org.standardnotes.notes.comms.data.EncryptedItem
import org.standardnotes.notes.comms.data.contentTypeFromString

class ContentTypeTest {

    @Test
    fun contentTypeFromString_ifNote_returnNote() {
        val item: EncryptedItem = EncryptedItem()
        item.contentType = "Note"

        assertEquals(ContentType.Note, contentTypeFromString(item.contentType))
    }

    @Test
    fun contentTypeFromString_ifTag_returnTag() {
        val item: EncryptedItem = EncryptedItem()
        item.contentType = "Tag"

        assertEquals(ContentType.Tag, contentTypeFromString(item.contentType))
    }
}