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

fun contentTypeFromString(str: String): ContentType? {
    if (str == "Note") return ContentType.Note
    if (str == "Tag") return ContentType.Tag
    return null
}