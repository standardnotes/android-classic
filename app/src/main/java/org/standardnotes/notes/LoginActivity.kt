package org.standardnotes.notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.AuthParamsResponse
import org.standardnotes.notes.comms.data.SigninResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.

        val mEmailSignInButton = findViewById(R.id.email_sign_in_button) as Button
        mEmailSignInButton.setOnClickListener {
            val email = (findViewById(R.id.email) as TextView).text.toString()
            val server = (findViewById(R.id.server) as TextView).text.toString()
            try {
                login_progress.visibility = View.VISIBLE
                SApplication.instance!!.valueStore.server = server
                SApplication.instance!!.resetComms()
                SApplication.instance!!.comms.api.getAuthParamsForEmail(email).enqueue(object : Callback<AuthParamsResponse> {
                    override fun onResponse(call: Call<AuthParamsResponse>, response: Response<AuthParamsResponse>) {
                        try {
                            val params = response.body()
                            val key = Crypt.generateKey((findViewById(R.id.password) as TextView).text.toString().toByteArray(Charsets.UTF_8),
                                    //Base64.decode(params.getPwSalt(), base64Flags),
                                    params.pwSalt.toByteArray(Charsets.UTF_8),
                                    params.pwCost!!,
                                    params.pwKeySize!!)
                            val fullHashedPassword = Crypt.bytesToHex(key)
                            val serverHashedPassword = fullHashedPassword.substring(0, fullHashedPassword.length / 2)
                            val mk = fullHashedPassword.substring(fullHashedPassword.length / 2)
                            SApplication.instance!!.comms.api.signin(email, serverHashedPassword).enqueue(object : Callback<SigninResponse> {
                                override fun onResponse(call: Call<SigninResponse>, response: Response<SigninResponse>) {
                                    if (response.isSuccessful) {
                                        SApplication.instance!!.valueStore.setTokenAndMasterKey(response.body().token, mk)
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
                            })
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onFailure(call: Call<AuthParamsResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                        login_progress.visibility = View.GONE
                    }
                })
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, getString(R.string.error_login), Toast.LENGTH_LONG).show()
                login_progress.visibility = View.GONE
            }
        }
    }


}

