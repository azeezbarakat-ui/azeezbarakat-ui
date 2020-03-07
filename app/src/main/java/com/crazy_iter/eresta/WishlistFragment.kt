package com.crazy_iter.eresta


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Adapters.ItemsAdapter
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.Models.ItemModel
import kotlinx.android.synthetic.main.fragment_wishlist.*
import org.json.JSONArray

@SuppressLint("ValidFragment")
class WishlistFragment(private val items: JSONArray) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        getWishlist()

        setup(items)

    }

    private fun setup(reqJSON: JSONArray) {
        val wishs = ArrayList<ItemModel>()
        for (i in 0 until reqJSON.length()) {
            val prod = reqJSON.getJSONObject(i)
            val price = try {
                prod.getDouble("product_price")
            } catch (e: Exception) {
                0.0
            }

            val rate = try {
                prod.getJSONArray("product_ratings")
            } catch (e: Exception) {
                JSONArray()
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
                    rate,
                    StaticsData.getShared(context!!, StaticsData.USER_ID).toInt(),

                    try {
                        CategoryModel(prod.getJSONObject("sub_category").getInt("id"),
                                prod.getJSONObject("sub_category").getString("sub_category_name"),
                                "")
                    } catch (e: Exception) {
                        null
                    },

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
            item.product_type = prod.getString("product_type")

            try {
                item.category = CategoryModel(prod.getJSONObject("category").getInt("id"),
                        prod.getJSONObject("category").getString("category_name"),
                        "")
                item.subSub = CategoryModel(prod.getJSONObject("sub_sub_category").getInt("id"),
                        prod.getJSONObject("sub_sub_category").getString("sub_sub_category_name"),
                        "")
            } catch (e: Exception) {
                Log.e("my request", e.toString())
            }

            Log.e("item_req", item.product_type)

            if (item.product_type.contains("request", true)) {
                wishs.add(item)
            }

        }



        try {
            wishlistPB.visibility = View.GONE
            wishlistRV.setHasFixedSize(true)
            wishlistRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            if (wishs.isEmpty()) {
                noWishlistTV.visibility = View.VISIBLE
            } else {
                wishlistRV.adapter = ItemsAdapter(context!!, wishs, myWish = true)
            }
        } catch (e: Exception) {
        }
    }

    private fun getWishlist() {
        val queue = Volley.newRequestQueue(context)
        val wishReq = object : JsonObjectRequest(APIs.REQUIST, null, {
            queue.cancelAll("wish")

            val wishs = ArrayList<ItemModel>()
            val reqJSON = it.getJSONArray("requests")
            for (i in 0 until reqJSON.length()) {
                val prod = reqJSON.getJSONObject(i)
                val price = try {
                    prod.getDouble("product_price")
                } catch (e: Exception) {
                    0.0
                }

                val rate = try {
                    prod.getJSONArray("product_ratings")
                } catch (e: Exception) {
                    JSONArray()
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
                        rate,
                        StaticsData.getShared(context!!, StaticsData.USER_ID).toInt(),

                        try {
                            CategoryModel(prod.getJSONObject("sub_category").getInt("id"),
                                    prod.getJSONObject("sub_category").getString("sub_category_name"),
                                    "")
                        } catch (e: Exception) {
                            null
                        },

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
                item.product_type = prod.getString("product_type")

                try {
                    item.category = CategoryModel(prod.getJSONObject("category").getInt("id"),
                            prod.getJSONObject("category").getString("category_name"),
                            "")
                    item.subSub = CategoryModel(prod.getJSONObject("sub_sub_category").getInt("id"),
                            prod.getJSONObject("sub_sub_category").getString("sub_sub_category_name"),
                            "")
                } catch (e: Exception) {
                    Log.e("my request", e.toString())
                }
            }

            try {
                wishlistPB.visibility = View.GONE
                wishlistRV.setHasFixedSize(true)
                wishlistRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                if (StaticsData.chats.isEmpty()) {
                    noWishlistTV.visibility = View.VISIBLE
                } else {
                    wishlistRV.adapter = ItemsAdapter(context!!, wishs, myWish = true)
                }
            } catch (e: Exception) {
            }

        }, {
            Log.e("wish", it.toString())
            queue.cancelAll("wish")
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context!!, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        wishReq.tag = "wish"
        queue.add(wishReq)

    }
}
