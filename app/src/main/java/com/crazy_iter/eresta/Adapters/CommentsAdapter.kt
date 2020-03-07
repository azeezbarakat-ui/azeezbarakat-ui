package com.crazy_iter.eresta.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.crazy_iter.eresta.Models.CommentModel
import com.crazy_iter.eresta.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private val context: Context,
                      private val comments: ArrayList<CommentModel>)
    : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.comment_item, p0, false))

    override fun getItemCount() = comments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = comments[position]
        try {
            Picasso.get()
                    .load(model.user.photo)
                    .placeholder(R.drawable.ic_user_colored)
                    .into(holder.civ)
        } catch (e: Exception) {}

        holder.username.text = model.user.name
        holder.comment.text = model.comment
        holder.date.text = model.date

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val civ: CircleImageView = itemView.findViewById(R.id.commentUserCIV)
        val username: TextView = itemView.findViewById(R.id.commentUserNameTV)
        val date: TextView = itemView.findViewById(R.id.commentDateTV)
        val comment: TextView= itemView.findViewById(R.id.commentTV)
    }

}