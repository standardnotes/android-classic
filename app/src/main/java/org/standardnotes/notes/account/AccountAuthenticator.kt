package org.standardnotes.notes.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.standardnotes.notes.LoginActivity
import org.standardnotes.notes.R

/**
 * Created by vorobyev on 2/9/17.
 */
class AccountAuthenticator(context: Context) : AbstractAccountAuthenticator(context) {

    private var  mContext: Context

    init {
        this.mContext = context
    }

    override fun getAuthTokenLabel(type: String?): String {
        return mContext.getString(R.string.app_name)
    }

    override fun confirmCredentials(p0: AccountAuthenticatorResponse?, p1: Account?, p2: Bundle?): Bundle {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateCredentials(p0: AccountAuthenticatorResponse?, p1: Account?, p2: String?, p3: Bundle?): Bundle {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuthToken(p0: AccountAuthenticatorResponse?, p1: Account?, p2: String?, p3: Bundle?): Bundle {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasFeatures(p0: AccountAuthenticatorResponse?, p1: Account?, p2: Array<out String>?): Bundle {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAccount(resp: AccountAuthenticatorResponse?, p1: String?, p2: String?, p3: Array<out String>?, p4: Bundle?): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, Intent(mContext, LoginActivity::class.java))
        return bundle
    }
}