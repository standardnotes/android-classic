package org.standardnotes.notes

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import org.standardnotes.notes.comms.CommsManager
import org.standardnotes.notes.store.NoteStore
import org.standardnotes.notes.store.ValueStore
import java.util.*

class SApplication : Application() {
    private val prefs: SharedPreferences by lazy { getSharedPreferences("app", Context.MODE_PRIVATE) }

    val commManagers = HashMap<Account, CommsManager>()

    fun commManager(server: String): CommsManager {
        return CommsManager(null, server)
    }
    fun commManager(account: Account): CommsManager {
        return commManagers.getOrPut(account, { CommsManager(account, null) })
    }

    val valueStores = HashMap<Account, ValueStore>()
    fun valueStore(account: Account): ValueStore {
        return valueStores.getOrPut(account, {ValueStore(this, account)})
    }

    val gson: Gson by lazy { GsonBuilder().registerTypeAdapter(DateTime::class.java, CommsManager.DateTimeDeserializer()).create() }

    val noteStores = HashMap<Account, NoteStore>()
    fun noteStore(account: Account): NoteStore {
        return noteStores.getOrPut(account, { NoteStore(account) })
    }
    fun resetComms() {
        commManagers.clear()
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

    fun account(intent: Intent?): Account? {
        var accounts = AccountManager.get(this).getAccountsByType(getString(R.string.account_type))
        var uuid: String? = intent?.getStringExtra("account")
        if (null == uuid) {
            uuid = prefs.getString(getString(R.string.account_default), uuid)
        }
        if (uuid != null) {
            for (account in accounts) {
                if (uuid.equals(valueStore(account).uuid)) {
                    return account
                }
            }
        }
        if (accounts.isNotEmpty()) {
            return accounts[0] // First account
        }
        return null
    }

    fun addAccount(accountID: String, server: String, email: String, masterKey: String?, token: String?): Boolean {
        val bundle = Bundle()
        val name = String.format("%s@%s", email, server)
        bundle.putString("email", email)
        bundle.putString("masterKey", masterKey)
        bundle.putString("server", server)
        bundle.putString("token", token)
        bundle.putString("uuid", accountID)
        val account = Account(name, getString(R.string.account_type))
        val result = AccountManager.get(this).addAccountExplicitly(account, "", bundle)
        if (result) {
            prefs.edit().putString(getString(R.string.account_default), accountID).commit()
        }
        return result
    }

    fun removeAccount(account: Account, activity: Activity) {
        val thread = Thread {
            kotlin.run {
                val future = AccountManager.get(this@SApplication).removeAccount(account, null, null)
                if(future.result) {
                    noteStore(account).deleteAll()
                    prefs.edit().remove(getString(R.string.account_default)).commit()
                    noteStores.clear()
                    valueStores.clear()
                    commManagers.clear()
                }

            }
        }
        thread.start()
    }

    fun changeAccount(account: Account) {
        prefs.edit().putString(getString(R.string.account_default), valueStore(account).uuid).commit()
    }

    fun getMasterKey(account: Account): String? {
        return AccountManager.get(this).getUserData(account, "masterKey")
    }
}
