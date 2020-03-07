package com.crazy_iter.eresta

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView

@SuppressLint("ViewConstructor")
class MorePhotosView(activity: Activity, uri: Uri)
    : View(activity) {

    val view = inflate(activity, R.layout.item_more_photo, null)!!

    init {
        val image = view.findViewById<ImageView>(R.id.itemMorePhotoIV)
        val cancel = view.findViewById<ImageView>(R.id.itemMorePhotoCancelIV)

        try {
            val bitmap = StaticsData.getResizedBitmap(MediaStore.Images.Media.getBitmap(activity.contentResolver, uri))
            image.setImageBitmap(bitmap)
        } catch (e: Exception) {}

        cancel.setOnClickListener {
            (activity as AddItemActivity).imagesUri.remove(uri)
            view.visibility = GONE
        }

    }

}