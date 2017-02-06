package org.standardnotes.notes

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.AuthParamsResponse
import org.standardnotes.notes.comms.data.SigninResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.LinearLayout
import android.widget.EditText



/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {



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
                login_progress.visibility = View.GONE
            }

            override fun onFailure(call: Call<SigninResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                login_progress.visibility = View.GONE
            }
        }

        email_sign_in_button.setOnClickListener {
            try {
                login_progress.visibility = View.VISIBLE
                SApplication.instance!!.valueStore.server = server.text.toString()
                SApplication.instance!!.resetComms()
                SApplication.instance!!.comms.api.getAuthParamsForEmail(email.text.toString()).enqueue(object : Callback<AuthParamsResponse> {
                    override fun onResponse(call: Call<AuthParamsResponse>, response: Response<AuthParamsResponse>) {
                        try {
                            val params = response.body()
                            if (!Crypt.isParamsSupported(this@LoginActivity, params)) {
                                login_progress.visibility = View.GONE
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
                        login_progress.visibility = View.GONE
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                login_progress.visibility = View.GONE
            }
        }
        sign_up.setOnClickListener {
            try {
                SApplication.instance!!.valueStore.server = server.text.toString()
                SApplication.instance!!.resetComms()

                val input = LayoutInflater.from(this).inflate(R.layout.view_password_confirm, null, false) as EditText
                AlertDialog.Builder(this).setTitle(R.string.prompt_confirm_password)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_ok, { dialogInterface, i ->
                            if (input.text.toString() == password.text.toString()) {
                                login_progress.visibility = View.VISIBLE
                                Crypt.doRegister(email.text.toString(), password.text.toString(), signInCallback)
                            } else {
                                Toast.makeText(this@LoginActivity, R.string.error_passwords_mismatch, Toast.LENGTH_LONG).show()
                            }
                        })
                        .setView(input)
                        .show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                login_progress.visibility = View.GONE
            }
        }
    }
}

