package com.crazy_iter.eresta.Adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.crazy_iter.eresta.MessagesActivity
import com.crazy_iter.eresta.Models.ChatModel
import com.crazy_iter.eresta.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(private val context: Context,
                   private val chats: ArrayList<ChatModel>)
    : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.chat_item, p0, false))

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = chats[position]
        try {
            Picasso.get()
                    .load(model.user(context)?.photo)
                    .placeholder(R.drawable.ic_user_colored)
                    .into(holder.chatCIV)
        } catch (e: Exception) {}

        holder.chatTV.text = model.user(context)?.name
        holder.chatLL.setOnClickListener {
            context.startActivity(
                    Intent(context,
                            MessagesActivity::class.java)
                            .putExtra("chat", position))
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatLL: LinearLayout = itemView.findViewById(R.id.chatItemLL)
        val chatCIV: CircleImageView = itemView.findViewById(R.id.chatCIV)
        val chatTV: TextView = itemView.findViewById(R.id.chatTV)
    }

}