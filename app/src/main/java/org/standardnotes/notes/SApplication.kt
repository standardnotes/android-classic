package org.standardnotes.notes

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.danlew.android.joda.JodaTimeAndroid
import org.acra.ReportingInteractionMode
import org.acra.annotation.ReportsCrashes
import org.acra.sender.HttpSender
import org.joda.time.DateTime
import org.standardnotes.notes.comms.CommsManager
import org.standardnotes.notes.store.NoteStore
import org.standardnotes.notes.store.ValueStore


@ReportsCrashes(
        httpMethod = HttpSender.Method.POST,
        reportType = HttpSender.Type.JSON,
        formUri = "http://android-acra.standardnotes.org:5984/acra-myapp/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "reporter2",
        formUriBasicAuthPassword = "reportit",
        sendReportsInDevMode = true,
        mode = ReportingInteractionMode.SILENT
        )
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
    val gson: Gson by lazy { GsonBuilder().registerTypeAdapter(DateTime::class.java, CommsManager.DateTimeDeserializer()).setPrettyPrinting().create() }
    val noteStore: NoteStore by lazy { NoteStore() }

    fun resetComms() {
        commsActual = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        JodaTimeAndroid.init(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        ACRA.init(this)
    }

    companion object {

        var instance: SApplication? = null
            private set
    }
}
