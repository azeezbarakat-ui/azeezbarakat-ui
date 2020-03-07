package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_reset_password.*
import org.json.JSONObject

class DialogResetPassword(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_reset_password)

        backResetPasswordIV.setOnClickListener { onBackPressed() }
        resetPasswordBTN.setOnClickListener {
            if (StaticsData.isEmail(resetPasswordEmailET.text.toString())) {
                sendResetRequest()
            }
        }

    }

    private fun sendResetRequest() {
        val emailJSON = JSONObject()
        emailJSON.put("email", resetPasswordEmailET.text.toString())

        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val resetRequest = JsonObjectRequest(Request.Method.POST, APIs.P_CREATE, emailJSON, {
            queue.cancelAll("reset")
            Toast.makeText(context, it.getString("message"), Toast.LENGTH_SHORT).show()
            loading.dismiss()
            dismiss()
        }, {
            queue.cancelAll("reset")
            loading.dismiss()
            Log.e("reset", it.toString())
        })
        resetRequest.tag = "reset"
        queue.add(resetRequest)
    }

}