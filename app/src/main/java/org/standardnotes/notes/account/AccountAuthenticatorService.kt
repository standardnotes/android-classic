package org.standardnotes.notes.account

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by vorobyev on 2/9/17.
 */
class AccountAuthenticatorService : Service() {

    override fun onBind(p0: Intent?): IBinder {
        return AccountAuthenticator(this).iBinder
    }

}