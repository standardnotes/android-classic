package org.standardnotes.notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import org.standardnotes.notes.comms.ExportUtil
import org.standardnotes.notes.comms.SyncManager

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction().replace(R.id.list,
                SettingsFragment()).commit()
        export.setOnClickListener { exportData() }
        feedback.setOnClickListener { startFeedbackIntent() }
        learnMore.setOnClickListener {
            val url = "https://standardnotes.org/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        logout.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle(R.string.action_logout)
                    .setMessage(R.string.prompt_are_you_sure)
                    .setPositiveButton(R.string.action_delete, { dialogInterface, i ->
                        logout()
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
        }
        login.visibility = if (app.valueStore.token == null) View.VISIBLE else View.GONE
        login.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
        version.text = "v" + packageManager.getPackageInfo(packageName, 0).versionName
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

    override fun onDestroy() {
        super.onDestroy()
        // TODO does this make sense here?
        ExportUtil.clearExports(this)
    }

    private fun exportData() {
        val listener: ExportUtil.ExportListener = object : ExportUtil.ExportListener {
            override fun onExportFailed() {
                Snackbar.make(root, R.string.error_fail_export, Snackbar.LENGTH_SHORT).show()
            }
        }
        if (radio_dec.isChecked)
            ExportUtil.exportDecrypted(this, listener)
        else
            ExportUtil.exportEncrypted(this, listener)
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
        SApplication.instance.clearData()
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