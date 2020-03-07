package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.Models.UserModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_send_first_message.*
import org.json.JSONObject
import java.util.*

class DialogFirstMessage(context: Context, val user: UserModel) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_send_first_message)

        firstTV.text = user.name
        firstMessageSendIV.setOnClickListener {
            if (firstMessageET.text.isNotEmpty()) {
                sendMessage(user.id, firstMessageET.text.toString())
            }
        }

    }

    private fun sendMessage(id: Int, text: String) {
        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val messageJSON = JSONObject()
        messageJSON.put("text_message", text)
        messageJSON.put("to_user_id", id)
        val messageRequest = object : JsonObjectRequest(Method.POST, APIs.MESSAGES, messageJSON, {
            queue.cancelAll("send")
            Log.e("send", it.toString())
            Toast.makeText(context, "Your message was sent successfully, you can continue in chats", Toast.LENGTH_LONG).show()
            loading.dismiss()
            dismiss()
        }, {
            loading.dismiss()
            queue.cancelAll("send")
            Toast.makeText(context, "Try again", Toast.LENGTH_LONG).show()
            Log.e("send", it.toString())
        })
        {
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

        messageRequest.tag = "send"
        queue.add(messageRequest)

    }

}