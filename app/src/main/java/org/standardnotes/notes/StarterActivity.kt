package org.standardnotes.notes

import android.accounts.AccountManager
import android.accounts.AccountManagerFuture
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class StarterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = SApplication.instance!!.account(intent)

        if (account != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            val manager = AccountManager.get(this)
            val future = manager.addAccount(getString(R.string.account_type), null, null, null, this, null, null)
            waitForFirstAccount(future)
        }
        finish()
    }

    inner class AccountWaitTask: AsyncTask<AccountManagerFuture<Bundle>, Any, Bundle>() {
        override fun doInBackground(vararg future: AccountManagerFuture<Bundle>): Bundle? {
            val bundle = future[0].result
            if (future[0].isDone)
                return bundle
            return null
        }

        override fun onPostExecute(result: Bundle?) {
            if (null != result) {
                startActivity(Intent(this@StarterActivity, MainActivity::class.java))
            }
        }
    }

    private fun waitForFirstAccount(future: AccountManagerFuture<Bundle>) {
        val task = AccountWaitTask()
        task.execute(future)
    }

}
