package com.crazy_iter.eresta.Models

import java.io.Serializable

class CategoryModel(var id: Int, var category_name: String, photo_link: String?) : Serializable {

    var photo_link: String = photo_link ?: ""
    var parentID: Int = 0

    override fun toString(): String {
        return this.category_name
    }
}