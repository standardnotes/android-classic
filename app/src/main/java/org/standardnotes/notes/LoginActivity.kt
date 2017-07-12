package org.standardnotes.notes

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.view_password_confirm.view.*
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.AuthParamsResponse
import org.standardnotes.notes.comms.data.SigninResponse
import org.standardnotes.notes.store.ValueStore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    var progressListener: ProgressListener? = null

    interface ProgressListener {
        fun onProgressShown()
        fun onProgressDismissed()
    }

    private fun notifyListener() {
        if (isInProgress()) progressListener?.onProgressShown() else progressListener?.onProgressDismissed()
    }

    fun isInProgress(): Boolean {
        return login_progress.visibility == View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val signInCallback: Callback<SigninResponse> = object : Callback<SigninResponse> {
            override fun onResponse(call: Call<SigninResponse>, response: Response<SigninResponse>) {
                if (response.isSuccessful) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                }
                hideProgress()
            }

            override fun onFailure(call: Call<SigninResponse>?, t: Throwable?) {
                Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                hideProgress()
            }
        }

        email_sign_in_button.setOnClickListener {
            try {
                showProgress()
                SApplication.instance.valueStore.server = server.text.toString()
                SApplication.instance.resetComms()
                SApplication.instance.comms.api.getAuthParamsForEmail(email.text.toString()).enqueue(object : Callback<AuthParamsResponse> {
                    override fun onResponse(call: Call<AuthParamsResponse>, response: Response<AuthParamsResponse>) {
                        try {
                            val params = response.body()
                            Crypt.doLogin(this@LoginActivity, email.text.toString(), password.text.toString(), params, signInCallback)
                            ValueStore(this@LoginActivity).authParams = params
                        } catch (e: Exception) {
                            Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }

                    }

                    override fun onFailure(call: Call<AuthParamsResponse>?, t: Throwable?) {
                        Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                        hideProgress()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                hideProgress()
            }
        }
        sign_up.setOnClickListener {
            try {
                showSignUpDialog(View.inflate(this, R.layout.view_password_confirm, null), signInCallback)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                hideProgress()
            }
        }

        password.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE && email_sign_in_button.isEnabled) {
                email_sign_in_button.performClick()
                return@OnEditorActionListener true
            }
            false
        })

        advancedPanel.setOnClickListener( {
            TransitionManager.beginDelayedTransition(advancedChild.parent as ViewGroup)
            advancedChild.visibility = if (advancedChild.visibility == View.GONE) View.VISIBLE else View.GONE
        })
        advancedPanel.paintFlags = advancedPanel.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkValidInput()
            }

            override fun afterTextChanged(s: Editable?) {
                //
            }
        }
        server.addTextChangedListener(textWatcher)
        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        server.setText(ValueStore(this).server)
        email.requestFocus()
    }

    private fun showSignUpDialog(view : View, signInCallback : Callback<SigninResponse>) {
        SApplication.instance.valueStore.server = server.text.toString()
        SApplication.instance.resetComms()

        val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setMessage(R.string.registration_confirmation)
                .setTitle(R.string.prompt_confirm_password)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_ok, { dialogInterface, i ->
                    if (view.confirm_password.text.toString() == password.text.toString()) {
                        showProgress()
                        Crypt.doRegister(email.text.toString(), password.text.toString(), signInCallback)
                    } else {
                        Toast.makeText(this@LoginActivity, R.string.error_passwords_mismatch, Toast.LENGTH_LONG).show()
                    }
                })
                .show()
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val okbutton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        view.confirm_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                okbutton.isEnabled = !view.confirm_password.text.isBlank()
            }

            override fun afterTextChanged(s: Editable?) {
                //
            }
        })
        view.confirm_password.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE && okbutton.isEnabled) {
                okbutton.performClick()
                return@OnEditorActionListener true
            }
            false
        })
        okbutton.isEnabled = false
    }

    private fun checkValidInput() {
        val valid: Boolean = !server.text.isBlank() && !email.text.isBlank() && !password.text.isBlank()
        email_sign_in_button.isEnabled = valid
        sign_up.isEnabled = valid
    }

    private fun showProgress() {
        // HACK to prevent activity restarts and mess up the UI state when logging in
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        server.isEnabled = false
        email.isEnabled = false
        password.isEnabled = false
        email_sign_in_button.isEnabled = false
        sign_up.isEnabled = false
        login_progress.visibility = View.VISIBLE
        notifyListener()
    }

    private fun hideProgress() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        server.isEnabled = true
        email.isEnabled = true
        password.isEnabled = true
        checkValidInput()
        login_progress.visibility = View.GONE
        notifyListener()
    }

}

