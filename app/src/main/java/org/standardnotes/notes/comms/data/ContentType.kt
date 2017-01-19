package org.standardnotes.notes.comms.data

/**
 * Created by carl on 19/01/17.
 */

enum class ContentType {
    Note, Tag;

    override fun toString(): String {
        return if (this == Note) "Note" else "Tag"
    }
}