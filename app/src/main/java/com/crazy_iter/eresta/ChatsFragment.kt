package com.crazy_iter.eresta

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Adapters.ChatsAdapter
import com.crazy_iter.eresta.Models.ChatModel
import com.crazy_iter.eresta.Models.MessageModel
import com.crazy_iter.eresta.Models.UserModel
import kotlinx.android.synthetic.main.fragment_chats.*

class ChatsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getMessages()

        chatsSRL.setOnRefreshListener {
            getMessages()
        }

    }

    private fun getMessages() {
        val queue = Volley.newRequestQueue(context)
        val messagesRequest = object : JsonObjectRequest(APIs.MESSAGES, null, {
            try {
                chatsSRL.isRefreshing = false
                queue.cancelAll("message")
                val allJSON = it.getJSONArray("messages_all")
                StaticsData.chats.clear()

                val messages = ArrayList<MessageModel>()

                for (i in 0 until allJSON.length()) {

                    val messageJSON = allJSON.getJSONObject(i)

                    val toUser = messageJSON.getJSONObject("to_user")
                    val fromUser = messageJSON.getJSONObject("from_user")

                    val message = MessageModel(
                            messageJSON.getInt("id"),
                            messageJSON.getString("text_message"),
                            messageJSON.getInt("did_read"),
                            messageJSON.getString("created_at"),
                            UserModel(toUser.getInt("id"),
                                    toUser.getString("name"),
//                                toUser.getString("photo_link"),
                                    ""
                            ),
                            UserModel(fromUser.getInt("id"),
                                    fromUser.getString("name"),
//                                fromUser.getString("photo_link"),
                                    ""
                            )
                    )

                    messages.add(message)

                }

                for (m in messages) {
                    var b = false

                    for (chat in StaticsData.chats) {

                        val id = chat.user(context!!)?.id
                        if (id == m.fromUser?.id || id == m.toUser.id) {
                            chat.messages.add(m)
                            b = true
                            break
                        }
                    }

                    if (!b) {
                        StaticsData.chats.add(ChatModel(m))
                    }

                }

                try {
                    chatsPB.visibility = View.GONE
                    chatsRV.setHasFixedSize(true)
                    chatsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    chatsRV.adapter = ChatsAdapter(context!!, StaticsData.chats)
                    if (StaticsData.chats.isEmpty()) {
                        noChatsTV.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                }
            } catch (e: Exception) {

            }
        }, {
            Log.e("message", it.toString())
            queue.cancelAll("message")
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context!!, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }
        messagesRequest.tag = "message"
        queue.add(messagesRequest)
    }

}
