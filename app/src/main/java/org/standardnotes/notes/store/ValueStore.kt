package org.standardnotes.notes.store

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import org.standardnotes.notes.BuildConfig
import org.standardnotes.notes.SApplication
import org.standardnotes.notes.comms.data.AuthParamsResponse

class ValueStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("values", Context.MODE_PRIVATE)

    fun setTokenAndMasterKey(token: String?, mk: String?, ak: String?) {
        prefs.edit().putString("masterKey", mk).putString("authKey", ak).putString("token", token).apply()
    }

    val masterKey: String?
        get() = prefs.getString("masterKey", null)

    val authKey: String?
        get() = prefs.getString("authKey", null)

    val token: String?
        get() = prefs.getString("token", null)

    var server: String?
        get() = prefs.getString("server", BuildConfig.SERVER_DEFAULT)
        set(value) { prefs.edit().putString("server", value).apply() }

    var email: String?
        get() = prefs.getString("email", "") // Set on login/registration
        set(value) { prefs.edit().putString("email", value).apply() }

    var syncToken: String?
        get() = prefs.getString("syncToken", null)
        set(token) { prefs.edit().putString("syncToken", token).apply() }

    var protocolVersion: String?
        get() {
            var savedvalue = prefs.getString("protocolVersion", null)
            if(savedvalue == null) {
                var params = authParams
                if(params != null && params.version != null) {
                    savedvalue = params.version
                }

                if(authKey != null) {savedvalue = "002"}
                else { savedvalue = "001" }

                protocolVersion = savedvalue
            }

            return savedvalue
        }
    set(value) { prefs.edit().putString("protocolVersion", value).apply() }

    var authParams: AuthParamsResponse?
        get() { return SApplication.instance.gson.fromJson(prefs.getString("authParams", null), object : TypeToken<AuthParamsResponse>() {}.type) }
        set(value) { prefs.edit().putString("authParams", SApplication.instance.gson.toJson(value)).apply() }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
