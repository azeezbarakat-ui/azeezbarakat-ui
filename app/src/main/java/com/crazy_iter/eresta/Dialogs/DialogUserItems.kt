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
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.Models.ItemModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_user_items.*
import org.json.JSONArray

class DialogUserItems(context: Context, private val id: Int) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_user_items)

        getFavSellers()

    }

    private fun getFavSellers() {
        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val productsRequest = object : JsonObjectRequest(APIs.PUF, null, {

            queue.cancelAll("seller")
            val followers = it.getJSONArray("productsfollowinguser")
            StaticsData.productsFollowers.clear()
            for (i in 0 until followers.length()) {
                if (followers.getJSONObject(i).getInt("id") != id) {
                    continue
                } else {
                    val fJSON = followers.getJSONObject(i).getJSONArray("products")
                    for (j in 0 until fJSON.length()) {
                        val prod = fJSON.getJSONObject(j)
                        val item = ItemModel(
                                prod.getInt("id"),
                                prod.getString("product_name"),
                                prod.getString("product_color"),
                                prod.getDouble("product_price"),
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
                                JSONArray(),
                                prod.getInt("user_id"),
                                CategoryModel(prod.getInt("sub_category_id"),
                                        "",
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
                        StaticsData.productsFollowers.add(item)
                    }
                }
            }

            if (StaticsData.productsFollowers.isEmpty()) {
                userItemsNoTV.visibility = View.VISIBLE
            } else {
                userItemsNoTV.visibility = View.GONE
                userItemsRV.setHasFixedSize(true)
                userItemsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                userItemsRV.adapter = ItemsAdapter(context, StaticsData.productsFollowers)
            }
            loading.dismiss()

        }, {
            Log.e("seller", it.toString())
            queue.cancelAll("seller")
            loading.dismiss()
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
            dismiss()
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

}