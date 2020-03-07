package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.ProfileActivity
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_validation.*
import org.json.JSONObject
import java.util.*

class DialogValidation(context: Context, private val intent: Int?, private val phone: String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_validation)

        backValidationIV.setOnClickListener { onBackPressed() }
        validateBTN.setOnClickListener {
            sendResetRequest()
        }
    }

    private fun nextActivity() {
        when (intent) {

            StaticsData.PROFILE -> {
                context.startActivity(Intent(context, ProfileActivity::class.java).putExtra("from", StaticsData.LOGIN))
                ownerActivity?.finish()
            }

            StaticsData.SELL -> {
                context.startActivity(Intent(context,
                        ProfileActivity::class.java
                ).putExtra("from", StaticsData.LOGIN))
                ownerActivity?.finish()
            }

            StaticsData.ITEM -> ownerActivity?.finish()

            null -> { dismiss() }

        }
    }

    private fun sendResetRequest() {
        val emailJSON = JSONObject()
        emailJSON.put("phone", phone)
        emailJSON.put("code", validationCodeET.text.toString())

        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val resetRequest = object : JsonObjectRequest(Method.POST, APIs.twoEnable, emailJSON, {
            queue.cancelAll("code")
            loading.dismiss()
            dismiss()
            nextActivity()
        }, {
            queue.cancelAll("code")
            loading.dismiss()
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
        resetRequest.tag = "code"
        queue.add(resetRequest)
    }

}