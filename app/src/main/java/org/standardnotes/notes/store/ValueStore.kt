package org.standardnotes.notes.store

import android.content.Context
import android.content.SharedPreferences

class ValueStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("values", Context.MODE_PRIVATE)

    fun setTokenAndMasterKey(token: String?, mk: String?) {
        prefs.edit().putString("masterKey", mk).putString("token", token).apply()
    }

    val masterKey: String?
        get() = prefs.getString("masterKey", null)

    val token: String?
        get() = prefs.getString("token", null)

    var server: String?
        get() = prefs.getString("server", null)
        set(value) { prefs.edit().putString("server", value).apply() }

    var syncToken: String?
        get() = prefs.getString("syncToken", null)
        set(token) { prefs.edit().putString("syncToken", token).apply() }
}
