package org.standardnotes.notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import org.standardnotes.notes.comms.SyncManager

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction().replace(R.id.list,
                SettingsFragment()).commit()
        feedback.setOnClickListener { startFeedbackIntent() }
        logout.setOnClickListener { logout() }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun startFeedbackIntent() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.feedback_email)))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, R.string.toast_no_email, Toast.LENGTH_LONG).show()
        }

    }

    private fun logout() {
        SApplication.instance!!.valueStore.setTokenAndMasterKey(null, null)
        SApplication.instance!!.noteStore.deleteAll()
        SyncManager.stopSyncTimer()
        startActivity(Intent(this, StarterActivity::class.java))
        finishAffinity()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.settings)
        }
    }
}