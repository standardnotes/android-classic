package org.standardnotes.notes.comms

import android.os.Handler
import android.util.Log
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.data.SyncItems
import org.standardnotes.notes.comms.data.UploadSyncItems
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

object SyncManager {

    interface SyncListener {
        fun onSyncStarted()

        fun onSyncCompleted(syncItems: SyncItems)

        fun onSyncFailed()
    }

    val TAG = SyncManager.javaClass.simpleName

    private val SYNC_INTERVAL = 30000L
    private val syncHandler: Handler = Handler()
    private val syncRunnable: Runnable = object : Runnable {
        override fun run() {
            SyncManager.sync()
            syncHandler.postDelayed(this, SYNC_INTERVAL)
        }
    }

    private var syncing: Boolean = false
    private val syncListeners: MutableList<WeakReference<SyncListener>> = mutableListOf()

    fun subscribe(listener: SyncListener) {
        syncListeners
                .filter { it.get() == listener }
                .forEach { return }
        syncListeners.add(WeakReference<SyncListener>(listener))
    }

    fun unsubscribe(listener: SyncListener) {
        syncListeners
                .filter { listener == it.get() }
                .forEach { syncListeners.remove(it) }
    }

    fun sync() {

        if (syncing) {
            return
        }

        syncing = true

        var iter = syncListeners.iterator()
        while (iter.hasNext()) {
            val listening = iter.next()
            if (listening.get() == null) {
                iter.remove()
                Log.w(TAG, "SyncListener is null, you may be missing a call to unsubscribe()")
            } else {
                listening.get().onSyncStarted()
            }
        }

        val uploadSyncItems = UploadSyncItems()
        uploadSyncItems.syncToken = SApplication.instance.valueStore.syncToken
        val dirtyItems = SApplication.instance.noteStore.toSave
        dirtyItems.map { Crypt.encrypt(it) }.forEach { uploadSyncItems.items.add(it) }
        SApplication.instance.comms.api.sync(uploadSyncItems).enqueue(object : Callback<SyncItems> {
            override fun onResponse(call: Call<SyncItems>, response: Response<SyncItems>) {

                SApplication.instance!!.noteStore.putItems(response.body())

                iter = syncListeners.iterator()
                while (iter.hasNext()) {
                    val listening = iter.next()
                    if (listening.get() == null) {
                        iter.remove()
                        Log.w(TAG, "SyncListener is null, you may be missing a call to unsubscribe()")
                    } else {
                        listening.get().onSyncCompleted(response.body())
                    }
                }

                syncing = false
            }

            override fun onFailure(call: Call<SyncItems>, t: Throwable) {
                iter = syncListeners.iterator()
                while (iter.hasNext()) {
                    val listening = iter.next()
                    if (listening.get() == null) {
                        iter.remove()
                        Log.w(TAG, "SyncListener is null, you may be missing a call to unsubscribe()")
                    } else {
                        listening.get().onSyncFailed()
                    }
                }

                syncing = false
            }
        })
    }

    fun startSyncTimer() {
        syncHandler.removeCallbacks(syncRunnable)
        syncHandler.postDelayed(syncRunnable, SYNC_INTERVAL)
    }

    fun stopSyncTimer() {
        syncHandler.removeCallbacks(syncRunnable)
    }

}