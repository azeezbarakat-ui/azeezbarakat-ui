package com.crazy_iter.eresta.Adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.crazy_iter.eresta.ImageActivity
import com.crazy_iter.eresta.R
import com.squareup.picasso.Picasso

class GalleryAdapter(private val context: Context, private val images: ArrayList<String>)
    : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) = ViewHolder(
            LayoutInflater.from(context)
                    .inflate(R.layout.item_photo, p0, false)
    )
    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            Picasso.get()
                    .load(images[position])
                    .into(holder.image)
        } catch (e: Exception) {

        }

        holder.image.setOnClickListener {
            context.startActivity(
                    Intent(context, ImageActivity::class.java)
                            .putExtra("image", images[position])
                            .putExtra("name", ""))
        }

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.galleryItemIV)
    }

}