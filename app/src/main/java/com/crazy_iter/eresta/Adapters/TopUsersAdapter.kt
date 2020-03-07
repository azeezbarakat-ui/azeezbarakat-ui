package com.crazy_iter.eresta.Adapters

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.crazy_iter.eresta.Dialogs.DialogProfile
import com.crazy_iter.eresta.Models.UserModel
import com.crazy_iter.eresta.R
import com.squareup.picasso.Picasso
import org.json.JSONObject

class TopUsersAdapter(private val context: Context, private val users: ArrayList<UserModel>)
    : RecyclerView.Adapter<TopUsersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_top_user, p0,false))

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = users[position]
        holder.name.text = model.name
        Log.e("top_photo", model.photo)
        try {
            Picasso.get()
                    .load(model.photo)
                    .placeholder(R.drawable.logo)
                    .into(holder.photo)
        } catch (e: Exception) { }

        holder.userCV.setOnClickListener {
            val itt = JSONObject()
            val itVal = JSONObject()
            itVal.put("name", model.name)
            itVal.put("photo_link", model.photo)
            itt.put("user_info", itVal)
            DialogProfile(context, model.id, itt, isTop = true).show()
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userCV: CardView = itemView.findViewById(R.id.userCV)
        val name: TextView = itemView.findViewById(R.id.userNameTV)
        val photo: ImageView = itemView.findViewById(R.id.userIV)
    }

}