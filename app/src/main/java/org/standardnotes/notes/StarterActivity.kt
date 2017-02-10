package org.standardnotes.notes

import android.accounts.AccountManager
import android.accounts.AccountManagerFuture
import android.app.Activity
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
            addNewAccount(this)
        }
        finish()
    }

}

fun addNewAccount(activity: Activity) {
    class AccountWaitTask: AsyncTask<AccountManagerFuture<Bundle>, Any, Bundle>() {
        override fun doInBackground(vararg future: AccountManagerFuture<Bundle>): Bundle? {
            val bundle = future[0].result
            if (future[0].isDone)
                return bundle
            return null
        }

        override fun onPostExecute(result: Bundle?) {
            if (null != result) {
                activity.startActivity(Intent(activity, MainActivity::class.java))
            }
        }
    }
    val manager = AccountManager.get(activity)
    val future = manager.addAccount(activity.getString(R.string.account_type), null, null, null, activity, null, null)
    val task = AccountWaitTask()
    task.execute(future)
}
