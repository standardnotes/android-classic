package org.standardnotes.notes

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime

import org.standardnotes.notes.comms.CommsManager
import org.standardnotes.notes.store.ValueStore

/**
 * Created by carl on 03/01/17.
 */

class SApplication : Application() {
    val comms: CommsManager by lazy { CommsManager("https://n3.standardnotes.org") }
    val valueStore: ValueStore by lazy { ValueStore(this) }
    val gson: Gson by lazy { GsonBuilder().registerTypeAdapter(DateTime::class.java, CommsManager.DateTimeDeserializer()).create() }

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
