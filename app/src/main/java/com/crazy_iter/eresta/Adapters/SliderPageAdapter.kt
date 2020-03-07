package com.crazy_iter.eresta.Adapters

import android.app.Activity
import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.crazy_iter.eresta.R
import java.util.*

class SliderPageAdapter(private val activity: Activity, private val images: ArrayList<String>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = layoutInflater.inflate(R.layout.layout_slider, container, false)
        val imSlider = view.findViewById<ImageView>(R.id.im_slider)

        try {
            Glide.with(activity)
                    .load(images[position])
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(imSlider)
        } catch (e: Exception) { }

        container.addView(view)

        return view
    }

    override fun getCount() = images.size


    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }
}