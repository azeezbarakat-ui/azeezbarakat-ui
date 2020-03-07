package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.Adapters.ItemsAdapter
import com.crazy_iter.eresta.ItemPreviewActivity
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.Models.ItemModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_profile.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DialogProfile(context: Context, private val userID: Int, private val it: JSONObject, private val isTop: Boolean = false) : Dialog(context) {

    private var favID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_profile)

        profileBackIV.setOnClickListener { onBackPressed() }

        Log.e("profile", it.toString())

        if (isTop) {
            profileRB.visibility = View.GONE
            profileFavBTN.visibility = View.GONE
            val info = it.getJSONObject("user_info")
            profileNameTV.text = info.getString("name")
            try {
                Picasso.get()
                        .load(info.getString("photo_link"))
                        .placeholder(R.drawable.ic_user_colored)
                        .into(profileCIV)
            } catch (e: Exception) {
            }
            getOffers()
            return
        }

        getFav()

        profileRB.setOnRatingBarChangeListener { _, rate, _ ->
            ratingUser(rate)
        }

        profileFavBTN.setOnClickListener {
            addToFavSellers()
        }

        setInfo()
        getOffers()
    }

    private fun setItems(myItemsJSON: JSONArray) {

        val currentDate = StaticsData.getCurrentDateTime()
        val offers = ArrayList<ItemModel>()

        for (i in 0 until myItemsJSON.length()) {
            val prod = myItemsJSON.getJSONObject(i)

            val price = try {
                prod.getDouble("product_price")
            } catch (e: Exception) {
                0.0
            }

            val item = ItemModel(
                    prod.getInt("id"),
                    prod.getString("product_name"),
                    prod.getString("product_color"),
                    price,
                    prod.get("product_weight"),
                    prod.get("size"),
                    prod.getString("model"),
                    prod.getString("product_description"),
                    prod.getInt("product_state"),
                    prod.getString("starting_date"),
                    prod.getString("end_date"),
                    prod.getString("product_address"),
                    prod.getDouble("product_latitude"),
                    prod.getDouble("product_longitude"),
                    prod.getString("guid"),
                    prod.getString("photo_link"),
                    prod.getJSONArray("photo_gallery_link"),
                    prod.getJSONArray("product_ratings"),
                    userID,
                    CategoryModel(prod.getJSONObject("sub_category").getInt("id"),
                            prod.getJSONObject("sub_category").getString("sub_category_name"),
                            ""),
                    "",
                    prod.getInt("unit_id"),
                    prod.getInt("currency_id"),
                    try {
                        prod.getJSONArray("orders")
                    } catch (e: Exception) {
                        JSONArray()
                    }
            )
            item.publishing_later = prod.getInt("publishing_later")
            item.allow_comments = prod.getInt("allow_comments")
            try {
                item.category = CategoryModel(prod.getJSONObject("category").getInt("id"),
                        prod.getJSONObject("category").getString("category_name"),
                        "")
                item.subSub = CategoryModel(prod.getJSONObject("sub_sub_category").getInt("id"),
                        prod.getJSONObject("sub_sub_category").getString("sub_sub_category_name"),
                        "")
            } catch (e: Exception) {

            }

            if (item.publishing_later != 1) {
                offers.add(item)
            }
        }
        profileOffersRV.setHasFixedSize(true)
        profileOffersRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        profileOffersRV.adapter = ItemsAdapter(context, offers)
    }

    private fun getOffers() {
        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val productsRequest = object : JsonObjectRequest(APIs.USER + userID, null, {

            queue.cancelAll("offers")
            setItems(it.getJSONObject("user_info").getJSONArray("products"))
            loading.dismiss()

        }, {
            Log.e("v", it.toString())
            queue.cancelAll("offers")
            StaticsData.retryFun(ownerActivity as ItemPreviewActivity, {
                getOffers()
            }, true)
        }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        productsRequest.tag = "offers"
        queue.add(productsRequest)
    }

    private fun getFav() {
        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val productsRequest = object : JsonObjectRequest(APIs.favoriteSellers + "?user_id=" + StaticsData.getShared(context!!, StaticsData.USER_ID), null, {

            queue.cancelAll("seller")
            val followers = it.getJSONArray("following")
            StaticsData.followers.clear()
            for (i in 0 until followers.length()) {
                val fJSON = followers.getJSONObject(i)
                if (fJSON.getInt("favorite_seller_id") == userID) {
                    favID = fJSON.getInt("id")
                }
            }

            loading.dismiss()

        }, {
            Log.e("seller", it.toString())
            queue.cancelAll("seller")
            StaticsData.retryFun(ownerActivity as ItemPreviewActivity, {
                getFav()
            }, true)
        }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        productsRequest.tag = "prod"
        queue.add(productsRequest)
    }

    private fun ratingUser(rate: Float) {
        val loading = DialogLoading(context)
        loading.show()
        val rateJSON = JSONObject()
        rateJSON.put("review", rate.roundToInt())
        rateJSON.put("user_review_id", userID)
        val queue = Volley.newRequestQueue(context)
        val rateReq = object : JsonObjectRequest(Method.POST, APIs.USER_RATE, rateJSON, {
            queue.cancelAll("rate")
            loading.dismiss()
        }, {
            Log.e("rate_error", it.toString())
            queue.cancelAll("rate")
            loading.dismiss()
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }

        rateReq.tag = "rate"
        queue.add(rateReq)
    }

    private fun removeFromFavSellers() {
        val loading = DialogLoading(context)
        loading.show()

        Log.e("remove_fav", "${APIs.favoriteSellers}/$favID")

        val queue = Volley.newRequestQueue(context)
        val favReq = object : JsonObjectRequest(Method.DELETE, "${APIs.favoriteSellers}/$favID", null, {
            queue.cancelAll("fav")
            loading.hide()
            profileFavBTN.text = "Add to favorite"
            profileFavBTN.setOnClickListener {
                addToFavSellers()
            }
        }, {
            Log.e("fav_error", it.toString())
            queue.cancelAll("fav")
            loading.hide()
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }

        favReq.tag = "fav"
        queue.add(favReq)
    }

    private fun addToFavSellers() {
        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val favJSON = JSONObject()
        favJSON.put("favorite_seller_id", userID)
        val favReq = object : JsonObjectRequest(Method.POST, APIs.favoriteSellers, favJSON, {
            queue.cancelAll("fav")
            loading.hide()
            profileFavBTN.text = "remove from favorite"

            getFav()

            profileFavBTN.setOnClickListener {
                removeFromFavSellers()
            }
        }, {
            Log.e("fav_error", it.toString())
            queue.cancelAll("fav")
            loading.hide()
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }

        favReq.tag = "fav"
        queue.add(favReq)
    }

    private fun setInfo() {
        val info = it.getJSONObject("user_info")
        profileNameTV.text = info.getString("name")
//        profileEmailTV.text = info.getString("email")
//        profilePhoneTV.text = info.getString("phone")
        try {
            Picasso.get()
                    .load(info.getString("photo_link"))
                    .placeholder(R.drawable.ic_user_colored)
                    .into(profileCIV)
        } catch (e: Exception) {
        }

        try {
            profileRB.rating = info.getString("user_rating").toFloat()
        } catch (e: Exception) {
            Log.e("rate", e.toString())
        }

        val f = info.getInt("followed")
        if (f == 0) {
            profileFavBTN.text = "Add to favorite"
            profileFavBTN.setOnClickListener {
                addToFavSellers()
            }
        } else {
            profileFavBTN.text = "remove from favorite"
            profileFavBTN.setOnClickListener {
                removeFromFavSellers()
            }
        }
    }
}