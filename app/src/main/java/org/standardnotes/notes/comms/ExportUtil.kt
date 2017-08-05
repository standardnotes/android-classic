package org.standardnotes.notes.comms

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import org.standardnotes.notes.R
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.data.*
import org.standardnotes.notes.store.ValueStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object ExportUtil {

    interface ExportListener {
        fun onExportFailed()
    }

    fun exportEncrypted(activity: Activity, listener: ExportListener?) {
        val encryptionVersion = SApplication.instance.valueStore.protocolVersion
        val exportItems = ExportItems()
        SApplication.instance.noteStore.notesList.map { Crypt.encrypt(it, encryptionVersion) }.forEach { exportItems.items.add(it) }
        SApplication.instance.noteStore.getAllTags(true).map { Crypt.encrypt(it, encryptionVersion) }.forEach { exportItems.items.add(it) }
        exportItems.authParams = ValueStore(activity).authParams

        export(activity, exportItems, listener)
    }

    fun exportDecrypted(activity: Activity, listener: ExportListener?) {
        val exportItems = ExportItems()
        SApplication.instance.noteStore.notesList.map { getPlaintextItem(it, ContentType.Note) }.forEach { exportItems.items.add(it) }
        SApplication.instance.noteStore.getAllTags(true).map { getPlaintextItem(it, ContentType.Tag) }.forEach { exportItems.items.add(it) }

        export(activity, exportItems, listener)
    }

    fun clearExports(context: Context) {
        val directory = File(context.filesDir.toString() + "/export")
        if (directory.isDirectory) {
            val children = directory.list()
            children.indices
                    .map { File(directory, children[it]) }
                    .forEach { it.delete() }
        }
    }

    private fun export(activity: Activity, exportItems: ExportItems, listener: ExportListener?) {
        val jsonString = SApplication.instance.gson.toJson(exportItems)
        val path = writeToFile(activity, jsonString)
        if (path != null) {
            shareFile(activity, path)
        } else {
            listener?.onExportFailed()
        }
    }

    private fun getPlaintextItem(source: EncryptableItem, contentType: ContentType): PlaintextItem {
        val content: EncryptableItem
        if (contentType == ContentType.Note) {
            content = Note()
            content.title = (source as Note).title
            content.text = source.text
        } else {
            content = Tag()
            content.title = (source as Tag).title
        }
        content.references = source.references
        content.dirty = null
        content.deleted = null

        val plaintextItem = PlaintextItem()
        plaintextItem.content = content
        plaintextItem.contentType = contentType.name
        plaintextItem.uuid = source.uuid
        plaintextItem.createdAt = source.createdAt
        plaintextItem.updatedAt = source.updatedAt

        return plaintextItem
    }

    private fun writeToFile(activity: Activity, data: String): String? {
        val path: String
        try {
            val filename = "SN Archive - " + SimpleDateFormat("EEE MMM d yyyy HH-mm-ss Z (z)", Locale.getDefault()).format(System.currentTimeMillis()) + ".txt"
            path = activity.filesDir.toString() + "/export/" + filename
            val file = File(path)
            file.parentFile.mkdirs()
            if (!file.exists()) {
                file.createNewFile()
            }
            val fos = FileOutputStream(file)
            fos.write(data.toByteArray())
            fos.close()
        } catch (e: IOException) {
            return null
        }
        return path
    }

    private fun shareFile(activity: Activity, path: String) {
        val fileUri = FileProvider.getUriForFile(activity, "org.standardnotes.notes.fileprovider", File(path))
        val shareIntent = ShareCompat.IntentBuilder.from(activity).setStream(fileUri).intent
        shareIntent.data = fileUri
        shareIntent.type = "message/rfc822"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.title_share_file)))
    }

}
