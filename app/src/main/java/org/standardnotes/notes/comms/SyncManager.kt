package org.standardnotes.notes.comms

import android.os.Handler
import android.util.Log
import org.acra.ACRA
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.data.AuthParamsResponse
import org.standardnotes.notes.comms.data.Note
import org.standardnotes.notes.comms.data.SyncItems
import org.standardnotes.notes.comms.data.UploadSyncItems
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

object SyncManager {

    interface SyncListener {
        fun onSyncStarted()

        fun onSyncCompleted()

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

    private var syncCall: Call<SyncItems>? = null
    private val syncListeners: MutableList<WeakReference<SyncListener>> = mutableListOf()

    fun subscribe(listener: SyncListener) {
        syncListeners
                .filter { it.get() == listener }
                .forEach { return }
        syncListeners.add(WeakReference<SyncListener>(listener))
    }

    fun unsubscribe(listener: SyncListener) {
        val thisListeners = syncListeners
                .filter { listener == it.get() }
        if (thisListeners.isEmpty()) {
            Log.w(TAG, "Cannot unsusbscribe, $listener is not subscribed")
        } else {
            thisListeners
                    .forEach { syncListeners.remove(it) }
        }
    }

    @Synchronized fun sync() {

        val existingCall = syncCall
        if (existingCall != null && !existingCall.isCanceled) {
            existingCall.cancel()
        }

        if (SApplication.instance.valueStore.token == null)
            return // Not logged in, sync impossible

        var iter = syncListeners.iterator()
        while (iter.hasNext()) {
            val listening = iter.next()
            if (listening.get() == null) {
                iter.remove()
                Log.w(TAG, "SyncListener is null, you may be missing a call to unsubscribe()")
            } else {
                listening.get()!!.onSyncStarted()
            }
        }

        val uploadSyncItems = UploadSyncItems()
        uploadSyncItems.syncToken = SApplication.instance.valueStore.syncToken

        val encryptionVersion = SApplication.instance.valueStore.protocolVersion

        val dirtyItems = SApplication.instance.noteStore.toSave
        dirtyItems.map { Crypt.encrypt(it, encryptionVersion) }.forEach { uploadSyncItems.items.add(it) }

        syncCall = SApplication.instance.comms.api.sync(uploadSyncItems)
        syncCall?.enqueue(object : Callback<SyncItems> {
            override fun onResponse(call: Call<SyncItems>, response: Response<SyncItems>) {

                if (response.isSuccessful) {
                    val putItemErrors = SApplication.instance.noteStore.putItems(response.body())

                    if (putItemErrors.isEmpty()) {
                        iter = syncListeners.iterator()
                        while (iter.hasNext()) {
                            val listening = iter.next()
                            if (listening.get() == null) {
                                iter.remove()
                                Log.w(TAG, "SyncListener is null, you may be missing a call to unsubscribe()")
                            } else {
                                listening.get()!!.onSyncCompleted()
                            }
                        }

                        syncCall = null
                    } else {
                        putItemErrors.forEach { ACRA.getErrorReporter().handleException(it) }
                        onFailure(call, Exception("Sync error"))
                    }
                } else {
                    val ex = Exception("sync failed " + response.errorBody().string())
                    ACRA.getErrorReporter().handleException(ex)
                    onFailure(call, ex)
                }
            }

            override fun onFailure(call: Call<SyncItems>, t: Throwable) {
                iter = syncListeners.iterator()
                while (iter.hasNext()) {
                    val listening = iter.next()
                    if (listening.get() == null) {
                        iter.remove()
                        Log.w(TAG, "SyncListener is null, you may be missing a call to unsubscribe()")
                    } else {
                        listening.get()!!.onSyncFailed()
                    }
                }

                syncCall = null
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