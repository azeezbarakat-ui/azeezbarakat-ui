package com.crazy_iter.eresta

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Adapters.MessagesAdapter
import com.crazy_iter.eresta.Models.MessageModel
import com.crazy_iter.eresta.Models.UserModel
import kotlinx.android.synthetic.main.activity_messages.*
import org.json.JSONObject
import java.util.*

class MessagesActivity : AppCompatActivity() {

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        messagesBackIV.setOnClickListener { onBackPressed() }
        position = intent.getIntExtra("chat", 0)
        messagesTV.text = StaticsData.chats[position].user(this)?.name

        messagesRV.setHasFixedSize(true)
        messagesRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        putMessage()

        messageSendIV.setOnClickListener {
            val text = messageET.text.toString()
            if (text.isNotEmpty()) {
                messageET.setText("")
                sendMessage(StaticsData.chats[position].user(this)?.id!!, text)
            }
        }

    }

    private fun putMessage() {
        messagesRV.adapter = MessagesAdapter(this, StaticsData.chats[position].messages)
    }

    private fun sendMessage(id: Int, text: String) {
        val queue = Volley.newRequestQueue(this)
        val messageJSON = JSONObject()
        messageJSON.put("text_message", text)
        messageJSON.put("to_user_id", id)
        val m = MessageModel(0, text, 0, Calendar.getInstance().time.toString(),
                StaticsData.chats[position].user(this)!!,
                UserModel(
                        StaticsData.getShared(this,
                                StaticsData.USER_ID).toInt(), "", ""))
        StaticsData.chats[position].messages.add(0, m)
        putMessage()
        val messageRequest = object : JsonObjectRequest(Method.POST, APIs.MESSAGES, messageJSON, {
            Log.e("send", it.toString())
        }, {
            Log.e("send", it.toString())
        })
        {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@MessagesActivity, StaticsData.TOKEN)}"
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
