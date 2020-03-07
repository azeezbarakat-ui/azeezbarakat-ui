package com.crazy_iter.eresta.Models

import android.content.Context
import com.crazy_iter.eresta.StaticsData
import java.io.Serializable

class MessageModel(val id: Int,
                   val text: String,
                   val did_read: Int,
                   val date: String,
                   val toUser: UserModel,
                   val fromUser: UserModel?) : Serializable {

    fun toMe(context: Context) =
            StaticsData
                    .getShared(context,
                            StaticsData.USER_ID).toInt() == this.toUser.id

}