package org.standardnotes.notes.store

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

class ValueStore(context: Context, var account: Account) {

    private var manager: AccountManager = AccountManager.get(context)

    fun setTokenAndMasterKey(server: String, token: String?, mk: String?) {
        manager.setUserData(account, "masterKey", mk)
        manager.setUserData(account, "token", token)
        manager.setUserData(account, "server", server)
    }

    val token: String?
        get() = manager.getUserData(account, "token")

    val server: String?
        get() = manager.getUserData(account, "server")

    val uuid: String?
        get() = manager.getUserData(account, "uuid")

    var syncToken: String?
        get() = manager.getUserData(account, "syncToken")
        set(token) { manager.setUserData(account, "syncToken", token) }
}
