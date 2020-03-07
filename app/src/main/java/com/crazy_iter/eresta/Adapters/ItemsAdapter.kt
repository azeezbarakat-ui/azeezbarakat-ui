package com.crazy_iter.eresta.Adapters

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.*
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Dialogs.DialogReOffer
import com.crazy_iter.eresta.Models.ItemModel
import com.squareup.picasso.Picasso

class ItemsAdapter(private val context: Context,
                   private val items: ArrayList<ItemModel>,
                   private val isMine: Boolean = false,
                   private val isDraft: Boolean = false,
                   private val type: Int = 0,
                   private val myActivity: MyItemsActivity? = null,
                   private val myWish: Boolean = false)
    : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.ad_item_linear, p0, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val model = items[position]

        try {
            Picasso.get()
                    .load(model.photo_link)
                    .into(holder.itemIV)

        } catch (err: Exception) {
        }
        holder.itemTV.text = model.name
        holder.itemDate.text = model.starting_date
        holder.itemPrice.text = model.price.toString()
        holder.itemLL.setOnClickListener {
            StaticsData.myWish = model
            context.startActivity(Intent(context, ItemPreviewActivity::class.java)
                    .putExtra("id", model.id)
                    .putExtra("isMine", isMine)
                    .putExtra("position", position)
                    .putExtra("type", type)
                    .putExtra("myWish", true)
            )
        }

        holder.deleteIV.visibility = if (isMine) View.VISIBLE else View.GONE
        holder.repeatIV.visibility = if (isDraft) View.VISIBLE else View.GONE

        holder.deleteIV.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("sure to delete ${model.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteItem(model.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show()
        }

        holder.repeatIV.setOnClickListener {
            DialogReOffer(context, model).show()
        }

    }

    private fun deleteItem(id: Int) {
        val loading = DialogLoading(context)
        loading.show()

        val queue = Volley.newRequestQueue(context)
        val deleteRequest = object : JsonObjectRequest(Method.DELETE, APIs.PRODUCTS + "/" + id, null, {

            Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
            queue.cancelAll("del")
            loading.hide()

            myActivity?.getInfo(type - 1)

//            holder.itemView.visibility = View.GONE
//            StaticsData.removeByID(id)

        }, {
            Log.e("del", it.toString())
            queue.cancelAll("del")
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
            loading.hide()
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
        deleteRequest.tag = "del"
        queue.add(deleteRequest)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemLL: LinearLayout = itemView.findViewById(R.id.adItemLL)
        val itemTV: TextView = itemView.findViewById(R.id.adTitleTV)
        val itemIV: ImageView = itemView.findViewById(R.id.adImageIV)
        val itemDate: TextView = itemView.findViewById(R.id.adDateTV)
        val itemPrice: TextView = itemView.findViewById(R.id.adPriceTV)
        val deleteIV: ImageView = itemView.findViewById(R.id.adDeleteIV)
        val repeatIV: ImageView = itemView.findViewById(R.id.adRepeatIV)

    }

}