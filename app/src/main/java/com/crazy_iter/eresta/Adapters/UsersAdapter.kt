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
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Dialogs.DialogUserItems
import com.crazy_iter.eresta.Models.UserModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter(private val context: Context, private val users: ArrayList<UserModel>)
    : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_user, p0,false))

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = users[position]
        holder.name.text = model.name
        try {
            Picasso.get()
                    .load(model.photo)
                    .placeholder(R.drawable.ic_user_colored)
                    .into(holder.photo)
        } catch (e: Exception) { }

        holder.userCV.setOnClickListener {
            DialogUserItems(context, model.id).show()
        }

        holder.cancel.setOnClickListener {
            removeFromFav(model.favID, holder)
        }

    }

    private fun removeFromFav(favID: Int, holder: ViewHolder) {
        val loading = DialogLoading(context)
        loading.show()

        val queue = Volley.newRequestQueue(context)
        val favReq = object : JsonObjectRequest(Method.DELETE, "${APIs.favoriteSellers}/$favID", null, {
            queue.cancelAll("fav")
            loading.hide()
            holder.userCV.visibility = View.GONE

        }, {
            Log.e("fav_error", it.toString())
            queue.cancelAll("fav")
            loading.hide()
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
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

        favReq.tag = "fav"
        queue.add(favReq)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userCV: CardView = itemView.findViewById(R.id.userCV)
        val name: TextView = itemView.findViewById(R.id.userNameTV)
        val photo: CircleImageView = itemView.findViewById(R.id.userCIV)
        val cancel: ImageView = itemView.findViewById(R.id.favCancelIV)
    }

}