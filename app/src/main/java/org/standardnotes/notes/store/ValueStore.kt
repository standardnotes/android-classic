package org.standardnotes.notes.store

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by carl on 03/01/17.
 */

class ValueStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("values", Context.MODE_PRIVATE)

    fun setTokenAndMasterKey(token: String?, mk: String?) {
        prefs.edit().putString("masterKey", mk).putString("token", token).apply()
    }

    val masterKey: String?
        get() = prefs.getString("masterKey", null)

    val token: String?
        get() = prefs.getString("token", null)

}
