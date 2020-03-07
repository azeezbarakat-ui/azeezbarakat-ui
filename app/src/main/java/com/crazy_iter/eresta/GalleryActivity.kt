package com.crazy_iter.eresta

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Adapters.GalleryAdapter
import com.crazy_iter.eresta.Dialogs.DialogLoading
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        galleryBackIV.setOnClickListener { onBackPressed() }

        getGallery()

    }

    private fun showPhotos(photos: ArrayList<String>) {

        if (photos.isEmpty()) {
            galleryRV.visibility = View.GONE
            noPhotoTV.visibility = View.VISIBLE
        } else {
            noPhotoTV.visibility = View.GONE
            galleryRV.visibility = View.VISIBLE
            galleryRV.setHasFixedSize(true)
            galleryRV.layoutManager = GridLayoutManager(this, 3)
            galleryRV.adapter = GalleryAdapter(this, photos)
        }

    }

    private fun getGallery() {
        val loading = DialogLoading(this)
        loading.show()

        val queue = Volley.newRequestQueue(this)
        val galleryReq = object : JsonObjectRequest(Method.GET, APIs.GALLERY, null, {

            loading.dismiss()

            val list = ArrayList<String>()
            val galleryJSONArray = it.getJSONArray("gallery")
            for (i in 0 until galleryJSONArray.length()) {

                val item = galleryJSONArray.getJSONObject(i)
                val p = item.getString("photo_link")
                if (p != "null") {
                    list.add(p)
                }

                val gall = item.getJSONArray("photo_gallery_link")
                for (j in 0 until gall.length()) {
                    list.add(gall.getString(j))
                }

            }
            showPhotos(list)

            queue.cancelAll("gal")

        }, {
            Log.e("gal", it.toString())
            queue.cancelAll("gal")
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
            loading.dismiss()
            finish()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@GalleryActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        galleryReq.tag = "gal"
        queue.add(galleryReq)
    }


}
