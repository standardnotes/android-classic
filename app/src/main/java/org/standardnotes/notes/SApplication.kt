package org.standardnotes.notes

import android.app.Application
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime

import org.standardnotes.notes.comms.CommsManager
import org.standardnotes.notes.store.NoteStore
import org.standardnotes.notes.store.ValueStore

class SApplication : Application() {
    private var commsActual: CommsManager? = null

    val comms: CommsManager
    get() {
        if (commsActual == null) {
            val server = valueStore.server
            if (server != null)
                commsActual = CommsManager(server)
        }
        return commsActual!! // Should not be called before there's a valueStore.server
    }
    val valueStore: ValueStore by lazy { ValueStore(this) }
    val gson: Gson by lazy { GsonBuilder().registerTypeAdapter(DateTime::class.java, CommsManager.DateTimeDeserializer()).create() }
    val noteStore: NoteStore by lazy { NoteStore() }

    fun resetComms() {
        commsActual = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        JodaTimeAndroid.init(this)
    }

    companion object {

        var instance: SApplication? = null
            private set
    }
}
