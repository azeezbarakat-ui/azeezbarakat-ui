package com.crazy_iter.eresta

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Dialogs.DialogLoading
import kotlinx.android.synthetic.main.activity_reset_password.*
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var token: String
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        backNewPasswordIV.setOnClickListener { onBackPressed() }
        try {
            val data = intent.getStringExtra("link")
            Log.e("reset_true", data!!)

            sendResetRequest(data.trimStart('/'))

        } catch (e: Exception) {
            Log.e("reset_link", e.message!!)
        }

        newPasswordLoginBTN.setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java)
                    .putExtra("next", StaticsData.RESTART)
                    .putExtra("reset", true))
        }

    }

    private fun sendResetRequest(url: String) {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val resetRequest = JsonObjectRequest(url, null, {
            queue.cancelAll("reset")
            loading.dismiss()
            token = it.getString("token")
            email = it.getString("email")

            newPasswordBTN.setOnClickListener {
                configNewPassword()
            }

        }, {
            Log.e("reset_password", it.toString())
            queue.cancelAll("reset")
            loading.dismiss()
            StaticsData.retryFun(this, {
                sendResetRequest(url)
            }, true)
        })

        resetRequest.tag = "reset"
        queue.add(resetRequest)
    }

    private fun configNewPassword() {

        if (!checkPasswords()) {
            return
        }

        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)

        val resetJSON = JSONObject()
        resetJSON.put("token", token)
        resetJSON.put("email", email)
        resetJSON.put("password", newPasswordTIL.editText?.text.toString())
        resetJSON.put("password_confirmation", newPassword2TIL.editText?.text.toString())

        val newPasswordRequest = JsonObjectRequest(Request.Method.POST, APIs.P_RESET, resetJSON, {
            Log.e("new_password", it.toString())
            loading.dismiss()

            newPasswordLL.visibility = View.GONE
            newPasswordLoginBTN.visibility = View.VISIBLE

        }, {
            Log.e("new_password", it.toString())
            loading.dismiss()
            newPasswordLL.visibility = View.GONE
            newPasswordLoginBTN.visibility = View.VISIBLE

//            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
        })

        newPasswordRequest.tag = "new_password"
        queue.add(newPasswordRequest)
    }

    private fun checkPasswords(): Boolean {
        if (newPasswordTIL.editText?.text.toString().length < 8) {
            newPasswordTIL.error = "Password have to be 8 characters at least"
            return false
        }

        if (newPassword2TIL.editText?.text.toString() != newPasswordTIL.editText?.text.toString()) {
            newPassword2TIL.error = "Passwords not match"
            return false
        }

        return true
    }

}
