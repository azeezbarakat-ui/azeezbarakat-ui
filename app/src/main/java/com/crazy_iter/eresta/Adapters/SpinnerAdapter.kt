package com.crazy_iter.eresta.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.crazy_iter.eresta.Models.CountryModel
import com.crazy_iter.eresta.Models.CurrencyModel
import com.crazy_iter.eresta.R
import com.squareup.picasso.Picasso

class SpinnerAdapter : ArrayAdapter<String> {

    private lateinit var models: ArrayList<Any>

    constructor(context: Context, models: Any) : super(context, R.layout.item_spinner_image) {
        this.models = models as ArrayList<Any>
    }

    override fun getCount(): Int {
        return models.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var holder = ViewHolder()
        val cv: View
        if (convertView == null) {
            val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cv = mInflater.inflate(R.layout.item_spinner_image, parent, false)
            holder.image = cv.findViewById(R.id.cSPIV)
            holder.title = cv.findViewById(R.id.cSPTV)
            cv.tag = holder
        } else {
            cv = convertView
            holder = cv.tag as ViewHolder
        }

        val model = models[position]
        try {
            if (model is CountryModel) {
                Picasso.get()
                        .load(model.image)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(holder.image)
                holder.title.text = model.title
            } else if (model is CurrencyModel) {
                holder.title.text = model.iso_code
            }
        } catch (ex: Exception) { }
        return cv
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    class ViewHolder {
        lateinit var image: ImageView
        lateinit var title: TextView
    }

}
