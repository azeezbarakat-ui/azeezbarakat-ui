package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_change_password.*
import org.json.JSONObject

class DialogChangePassword(context: Context, val email: String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_change_password)

        backCPasswordIV.setOnClickListener { onBackPressed() }

        cPasswordBTN.setOnClickListener {
            if (isCheckPasswords()) {
                sendChangeRequest()
            }
        }

    }

    private fun isCheckPasswords(): Boolean {

        if (cpNewPasswordET.editText?.text.toString().length < 8) {
            cpNewPasswordET.error = "Password have to be 8 characters at least"
            return false
        }

        if (cpNewPasswordET2.editText?.text.toString() != cpNewPasswordET.editText?.text.toString()) {
            cpNewPasswordET2.error = "Passwords not match"
            return false
        }

        return true
    }

    private fun sendChangeRequest() {
        val loading = DialogLoading(context)
        loading.show()

        val cpJSON = JSONObject()
        cpJSON.put("email", email)
        cpJSON.put("password", cpNewPasswordET.editText?.text.toString())
        cpJSON.put("password_confirmation", cpNewPasswordET2.editText?.text.toString())

        val queue = Volley.newRequestQueue(context)
        val resetRequest = object : JsonObjectRequest(Method.POST, APIs.changepassword, cpJSON, {

            Log.e("change", it.toString())
            Toast.makeText(context, "Password Changed Successfully", Toast.LENGTH_SHORT).show()
            queue.cancelAll("cp")
            loading.dismiss()
            dismiss()

        }, {

            queue.cancelAll("cp")
            loading.dismiss()
            dismiss()
            Log.e("cp", it.toString())

        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }
        resetRequest.tag = "cp"
        queue.add(resetRequest)
    }

}