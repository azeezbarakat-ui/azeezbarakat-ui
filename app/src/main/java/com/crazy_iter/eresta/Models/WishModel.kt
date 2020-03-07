package com.crazy_iter.eresta.Models

import java.io.Serializable

class WishModel(val id: Int,
                val title: String,
                val type: String,
                val image: String,
                val category: CategoryModel?) : Serializable