package com.crazy_iter.eresta.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.crazy_iter.eresta.Models.MessageModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData

class MessagesAdapter(private val context: Context,
                      private val messages: ArrayList<MessageModel>)
    : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.message_item, p0, false))

    override fun getItemCount() = messages.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = messages[position]

        holder.messageDateTV.text = model.date.split(" ")[0] + " | " + model.date.split(" ")?.get(1)

        holder.messageTV.text = model.text
        if (model.fromUser?.id != StaticsData.getShared(context, StaticsData.USER_ID).toInt()) {
            holder.messageLL.gravity = Gravity.END
            holder.messageTV.setBackgroundResource(R.drawable.message_background_resive)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageLL: LinearLayout = itemView.findViewById(R.id.messageLL)
        val messageDateTV: TextView = itemView.findViewById(R.id.messageDateTV)
        val messageTV: TextView = itemView.findViewById(R.id.messageTV)
    }

}