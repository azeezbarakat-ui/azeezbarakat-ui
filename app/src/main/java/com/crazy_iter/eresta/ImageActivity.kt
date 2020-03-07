package com.crazy_iter.eresta

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        imageBackIV.setOnClickListener { onBackPressed() }


        val image = intent.getStringExtra("image")
        imageNameTV.text = intent.getStringExtra("name")

        try {
            Picasso.get()
                    .load(image)
                    .into(imageIV)
        } catch (e: Exception) {

        }

    }
}
