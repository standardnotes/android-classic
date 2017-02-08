package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.AuthParamsResponse
import org.standardnotes.notes.comms.data.SigninResponse
import org.standardnotes.notes.store.ValueStore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.

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

            override fun onFailure(call: Call<SigninResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                hideProgress()
            }
        }

        email_sign_in_button.setOnClickListener {
            try {
                showProgress()
                SApplication.instance!!.valueStore.server = server.text.toString()
                SApplication.instance!!.resetComms()
                SApplication.instance!!.comms.api.getAuthParamsForEmail(email.text.toString()).enqueue(object : Callback<AuthParamsResponse> {
                    override fun onResponse(call: Call<AuthParamsResponse>, response: Response<AuthParamsResponse>) {
                        try {
                            val params = response.body()
                            if (!Crypt.isParamsSupported(this@LoginActivity, params)) {
                                hideProgress()
                                return
                            }
                            Crypt.doLogin(email.text.toString(), password.text.toString(), params, signInCallback)
                        } catch (e: Exception) {
                            Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }

                    }

                    override fun onFailure(call: Call<AuthParamsResponse>, t: Throwable) {
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
                SApplication.instance!!.valueStore.server = server.text.toString()
                SApplication.instance!!.resetComms()

                val layout = LayoutInflater.from(this).inflate(R.layout.view_password_confirm, null, false)
                val input = layout.findViewById(R.id.password) as EditText
                val dialog = AlertDialog.Builder(this).setTitle(R.string.prompt_confirm_password)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_ok, { dialogInterface, i ->
                            if (input.text.toString() == password.text.toString()) {
                                showProgress()
                                Crypt.doRegister(email.text.toString(), password.text.toString(), signInCallback)
                            } else {
                                Toast.makeText(this@LoginActivity, R.string.error_passwords_mismatch, Toast.LENGTH_LONG).show()
                            }
                        })
                        .setView(layout)
                        .show()
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                val okbutton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        //
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        okbutton.isEnabled = !input.text.isBlank()
                    }

                    override fun afterTextChanged(s: Editable?) {
                        //
                    }
                })
                input.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE && okbutton.isEnabled) {
                        okbutton.performClick()
                        return@OnEditorActionListener true
                    }
                    false
                })
                okbutton.isEnabled = false
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

    private fun checkValidInput() {
        val valid: Boolean = !server.text.isBlank() && !email.text.isBlank() && !password.text.isBlank()
        email_sign_in_button.isEnabled = valid
        sign_up.isEnabled = valid
    }

    private fun showProgress() {
        // TODO lock orientation changes?
        server.isEnabled = false
        email.isEnabled = false
        password.isEnabled = false
        email_sign_in_button.isEnabled = false;
        sign_up.isEnabled = false
        login_progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        // TODO unlock orientation changes?
        server.isEnabled = true
        email.isEnabled = true
        password.isEnabled = true
        checkValidInput()
        login_progress.visibility = View.GONE
    }

}

