package org.standardnotes.notes.comms.data.export

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.PlaintextItem
import org.standardnotes.notes.comms.data.Tag
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ExportUtil {

    interface ExportListener {
        fun onExportSucceeded()

        fun onExportFailed()
    }

    fun exportEncrypted(activity: Activity, listener: ExportListener?) {

        val exportItems = ExportItemsEncrypted()
        val notes = SApplication.instance!!.noteStore.notesList
        val tags = SApplication.instance!!.noteStore.getAllTags()
        notes.map { Crypt.encrypt(it) }.forEach { exportItems.items.add(it) }
        tags.map { Crypt.encrypt(it) }.forEach { exportItems.items.add(it) }

        SApplication.instance!!.comms.api.getAuthParamsForEmail(SApplication.instance!!.valueStore.email).enqueue(object : Callback<AuthParamsResponse> {
            override fun onResponse(call: Call<AuthParamsResponse>, response: Response<AuthParamsResponse>) {

                val params = response.body()

                if (!Crypt.isParamsSupported(activity, params)) {
                    // TODO error callback
                    return
                }

                exportItems.authParams = params

                val jsonString = SApplication.instance!!.gson.toJson(exportItems)

                val path = writeToFile(activity, jsonString)
                if (path != null) {
                    shareFile(activity, path)
                    listener?.onExportSucceeded()
                } else {
                    listener?.onExportFailed()
                }
            }

            override fun onFailure(call: Call<AuthParamsResponse>, t: Throwable) {
                listener?.onExportFailed()
            }
        })
    }

    fun exportPlaintext(activity: Activity, listener: ExportListener?) {
        val exportItems = ExportItemsPlaintext()
        val notes = SApplication.instance!!.noteStore.notesList
        val tags = SApplication.instance!!.noteStore.getAllTags()
        notes.map { getPlaintextNote(it) }.forEach { exportItems.items.add(it) }
        tags.map { getPlaintextTag(it) }.forEach { exportItems.items.add(it) }

        val jsonString = SApplication.instance!!.gson.toJson(exportItems)

        val path = writeToFile(activity, jsonString)
        if (path != null) {
            shareFile(activity, path)
            listener?.onExportSucceeded()
        } else {
            listener?.onExportFailed()
        }
    }

    private fun getPlaintextNote(note: Note): PlaintextItem {
        val justContent = Note()
        justContent.title = note.title
        justContent.text = note.text
        justContent.references = note.references
        justContent.dirty = null
        justContent.deleted = null

        val plaintextItem = PlaintextItem()
        plaintextItem.content = justContent
        plaintextItem.contentType = "Note"
        plaintextItem.uuid = note.uuid
        plaintextItem.createdAt = note.createdAt
        plaintextItem.updatedAt = note.updatedAt

        return plaintextItem
    }

    private fun getPlaintextTag(tag: Tag): PlaintextItem {
        val justContent = Tag()
        justContent.title = tag.title
        justContent.references = tag.references
        justContent.dirty = null
        justContent.deleted = null

        val plaintextItem = PlaintextItem()
        plaintextItem.content = justContent
        plaintextItem.contentType = "Note"
        plaintextItem.uuid = tag.uuid
        plaintextItem.createdAt = tag.createdAt
        plaintextItem.updatedAt = tag.updatedAt

        return plaintextItem
    }

    private fun writeToFile(activity: Activity, data: String): String? {
        val path: String
        try {
            val filename = "SN Archive - " + SimpleDateFormat("EEE MMM d yyyy HH-mm-ss Z (z)", Locale.getDefault()).format(System.currentTimeMillis()) + ".json"
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
        activity.startActivity(Intent.createChooser(shareIntent, "Share File"), null)
    }

    // unused TODO should we delete the file somewhere, like onActivityResult?
    private fun deleteFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}
