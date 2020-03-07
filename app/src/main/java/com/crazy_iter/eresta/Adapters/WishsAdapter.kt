package com.crazy_iter.eresta.Adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.crazy_iter.eresta.ItemPreviewActivity
import com.crazy_iter.eresta.Models.WishModel
import com.crazy_iter.eresta.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class WishsAdapter(private val context: Context,
                   private val wishs: ArrayList<WishModel>)
    : RecyclerView.Adapter<WishsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_wishlist, p0, false))

    override fun getItemCount() = wishs.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = wishs[position]
        try {
            Picasso.get()
                    .load(model.image)
                    .placeholder(R.drawable.logo)
                    .into(holder.wishCIV)
        } catch (e: Exception) {}

        holder.title.text = model.title
//        holder.type.text = model.type
        holder.type.visibility = View.GONE
        holder.category.text = model.category?.category_name
        holder.wishCV.setOnClickListener {
            context.startActivity(Intent(context, ItemPreviewActivity::class.java)
                    .putExtra("wish", model)
                    .putExtra("myWish", true)
            )
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wishCV = itemView.findViewById<CardView>(R.id.wishCV)
        val title = itemView.findViewById<TextView>(R.id.wishTitleTV)
        val type = itemView.findViewById<TextView>(R.id.wishTypeTV)
        val category = itemView.findViewById<TextView>(R.id.wishCategoryTV)
        val wishCIV = itemView.findViewById<CircleImageView>(R.id.wishCIV)
    }

}