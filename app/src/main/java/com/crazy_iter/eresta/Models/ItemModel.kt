package com.crazy_iter.eresta.Models

import org.json.JSONArray

class ItemModel() {

    var id: Int = 0
    lateinit var name: String
    var color: String? = null
    var price: Double? = null
    var weight: Any? = null
    var size: Any? = null
    var model: String? = null
    lateinit var description: String
    var state: Int = 0
    lateinit var starting_date: String
    lateinit var end_date: String
    lateinit var product_address: String
    var product_latitude: Double = 0.0
    var product_longitude: Double = 0.0
    lateinit var guid: String
    var photo_link: String? = ""
    lateinit var photo_gallery_link: JSONArray
    lateinit var ratingJSON: JSONArray
    var userID: Int = 0
    var unitID: Int = 0
    var currencyID: Int = 0
    var subCategory: CategoryModel? = null
    lateinit var email: String
    lateinit var ordersList: ArrayList<OrderModel>
    var publishing_later: Int = 0
    var allow_comments: Int = 0
    var product_type: String = ""

    var subSub: CategoryModel? = null
    var category: CategoryModel? = null

    var price_type: String = ""

    constructor(id: Int,
                name: String,
                color: String?,
                price: Double,
                weight: Any?,
                size: Any?,
                model: String?,
                description: String,
                state: Int,
                starting_date: String,
                end_date: String,
                product_address: String,
                product_latitude: Double,
                product_longitude: Double,
                guid: String,
                photo_link: String,
                photo_gallery_link: JSONArray,
                ratingJSON: JSONArray,
                userID: Int,
                categoryModel: CategoryModel?,
                email: String,
                unitID: Int,
                currencyID: Int,
                orders: JSONArray) : this() {
        this.id = id
        this.name = name
        this.price = price
        this.color = color ?: ""
        this.weight = weight
        this.size = size
//        try {
//        } catch (e: Exception) {
//            this.weight = 0.0
//        }
//        Log.e("weight", this.weight.toString())
//
//        try {
//            this.size = size as Double
//        } catch (e: Exception) {
//            this.size = 0.0
//        }
        this.model = model ?: ""
        this.description = description
        this.state = state
        this.starting_date = starting_date
        this.end_date = end_date
        this.product_address = product_address
        this.product_latitude = product_latitude
        this.product_longitude = product_longitude
        this.guid = guid
        this.photo_link = photo_link
        this.photo_gallery_link = photo_gallery_link
        this.ratingJSON = ratingJSON
        this.userID = userID
        this.subCategory = categoryModel
        this.email = email
        this.unitID = unitID
        this.currencyID = currencyID
        this.ordersList = setupOrders(orders)
    }

    private fun setupOrders(orders: JSONArray): ArrayList<OrderModel> {
        val ordersList = ArrayList<OrderModel>()
        for (i in 0 until orders.length()) {
            val orderJSON = orders.getJSONObject(i)
            val userJSON = orderJSON.getJSONObject("user")
            ordersList.add(
                    OrderModel(
                            orderJSON.getInt("id"),
                            orderJSON.getInt("user_id"),
                            orderJSON.getInt("product_id"),
                            orderJSON.getString("received_date"),
                            try {
                                orderJSON.getInt("approved")
                            } catch (e: Exception) {
                                -1
                            },
                            "",
                            try {
                                UserModel(
                                        userJSON.getInt("id"),
                                        userJSON.getString("name"),
                                        userJSON.getString("photo_link"),
                                        userJSON.getString("email"),
                                        try {
                                            userJSON.getDouble("address_longitude")
                                        } catch (e: Exception) {
                                            0.0
                                        },
                                        try {
                                            userJSON.getDouble("address_latitude")
                                        } catch (e: Exception) {
                                            0.0
                                        },
                                        userJSON.getString("address_address")
                                )
                            } catch (e: Exception) {
                                null
                            }
                    )
            )
        }
        return ordersList
    }

    fun getOrders(): ArrayList<OrderModel> {
        return ordersList
    }

    fun getGalleryArray(): ArrayList<String> {
        val photos = ArrayList<String>()
        for (i in 0 until photo_gallery_link.length()) {
            photos.add(photo_gallery_link.getString(i))
        }
        return photos
    }

    fun getRating(): Double {
        var rating = 0.0
        for (j in 0 until ratingJSON.length()) {
            rating += ratingJSON.getJSONObject(j).getDouble("product_rating")
        }
        return (rating + myLastRate) / (ratingJSON.length() + 1)
    }

    var myLastRate = 0

}