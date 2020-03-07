package com.crazy_iter.eresta.Models

import android.content.Context
import java.io.Serializable

class ChatModel(messageModel: MessageModel) : Serializable {

    var messages: ArrayList<MessageModel> = ArrayList()
    init {
        messages.add(messageModel)
    }

    fun user(context: Context) = if (messages[0].toMe(context)) {
        messages[0].fromUser
    } else {
        messages[0].toUser
    }

}