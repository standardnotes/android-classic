package org.standardnotes.notes.comms

import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.SyncItems
import org.standardnotes.notes.comms.data.UploadSyncItems
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SyncManager {

    interface SyncListener {
        fun onSyncCompleted(notes: List<Note>)

        fun onSyncFailed()
    }

    private val syncListeners: MutableList<SyncListener> = mutableListOf()

    fun subscribe(listener: SyncListener) {
        if (!syncListeners.contains(listener)) {
            syncListeners.add(listener)
        }
    }

    fun unsubscribe(listener: SyncListener) {
        syncListeners
                .filter { listener == it }
                .forEach { syncListeners.remove(it) }
    }

    fun sync() {
        val uploadSyncItems = UploadSyncItems()
        uploadSyncItems.syncToken = SApplication.instance!!.valueStore.syncToken
        val dirtyItems = SApplication.instance!!.noteStore.toSave
        dirtyItems.map { Crypt.encrypt(it) }.forEach { uploadSyncItems.items.add(it) }
        SApplication.instance!!.comms.api.sync(uploadSyncItems).enqueue(object : Callback<SyncItems> {
            override fun onResponse(call: Call<SyncItems>, response: Response<SyncItems>) {
                SApplication.instance!!.noteStore.putItems(response.body())
                val notes = SApplication.instance!!.noteStore.notesList
                for (listening: SyncListener in syncListeners) {
                    listening.onSyncCompleted(notes)
                }
            }

            override fun onFailure(call: Call<SyncItems>, t: Throwable) {
                for (listening: SyncListener in syncListeners) {
                    listening.onSyncFailed()
                }
            }
        })
    }

}