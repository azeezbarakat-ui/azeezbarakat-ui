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
import kotlinx.android.synthetic.main.dialog_manage_paypal.*
import org.json.JSONObject

class DialogManagePaypal(context: Context, val infoJSON: JSONObject, val email: String) : Dialog(context) {

    lateinit var paypalEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_manage_paypal)

        backManagePaypalIV.setOnClickListener { onBackPressed() }

        paypalEmailTIL.editText?.setText(email)
        paypalEmail = email

        paypalDoneIV.setOnClickListener {
            if (StaticsData.isEmail(paypalEmailTIL.editText?.text.toString())) {
                editRequest()
            } else {
                Toast.makeText(context, "Check your paypal email", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun editRequest() {
        val loading = DialogLoading(context)
        loading.show()

        val editJSON = JSONObject()
        editJSON.put("id", infoJSON.getString("id"))
        editJSON.put("email_paypal", paypalEmailTIL.editText?.text.toString())
        editJSON.put("account_type", infoJSON.getString("account_type"))

        Log.e("edit", editJSON.toString())

        val queue = Volley.newRequestQueue(context)
        val editRequest = object : JsonObjectRequest(Method.PUT, APIs.USER + infoJSON.getString("id"), editJSON, {

            Log.e("edit", it.toString())
            Toast.makeText(context, "Edited!", Toast.LENGTH_SHORT).show()
            paypalEmail = paypalEmailTIL.editText?.text.toString()
            loading.dismiss()
            dismiss()

        }, {
            Log.e("edit", it.toString())
            queue.cancelAll("edit")
            loading.hide()
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
        editRequest.tag = "edit"
        queue.add(editRequest)
    }

}