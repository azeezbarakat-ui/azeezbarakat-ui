package com.crazy_iter.eresta

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView

@SuppressLint("ViewConstructor")
class ViewTag(context: Context, name: String) : View(context) {

    val view: View = inflate(context, R.layout.item_tag, null)
    var isSelectedTag: Boolean = false

    init {
        val tagTV = this.view.findViewById<TextView>(R.id.tagTV)
        tagTV.text = name
    }

}
