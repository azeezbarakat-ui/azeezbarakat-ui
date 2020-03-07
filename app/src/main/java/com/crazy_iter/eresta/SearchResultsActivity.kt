package com.crazy_iter.eresta

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.crazy_iter.eresta.Adapters.ItemsAdapter
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.Models.ItemModel
import kotlinx.android.synthetic.main.activity_search_results.*
import org.json.JSONArray
import org.json.JSONObject

class SearchResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        resultsBackIV.setOnClickListener { onBackPressed() }
        resultsRV.setHasFixedSize(true)
        resultsRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val tag = intent.getStringExtra("tag") ?: ""
        if (tag.isEmpty()) {
            normalSetup()
        } else {
            Log.i("tag", tag)
            val items = ArrayList<ItemModel>()
            for (item in StaticsData.items) {
                if (item.description.contains(tag, true)) {
                    items.add(item)
                }
                Log.i("desc", item.description)
            }

            resultsRV.adapter = ItemsAdapter(this, items, isMine = false)

        }

    }

    private fun normalSetup() {
        val ii = JSONObject(intent.getStringExtra("products")!!)
        val results = ii.getJSONArray("products")

        if (results.length() == 0) {
            noResultsTV.visibility = View.VISIBLE
            resultsRV.visibility = View.GONE
        } else {
            noResultsTV.visibility = View.GONE
            resultsRV.visibility = View.VISIBLE
        }

        val items = ArrayList<ItemModel>()
        for (i in 0 until results.length()) {
            val pro = results.getJSONArray(i)
            for (p in 0 until pro.length()) {
                val prod = pro.getJSONObject(p)
                try {
                    if (prod.getInt("publishing_later") == 0) {
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
                                prod.getJSONArray("product_ratings"),
                                prod.getJSONObject("user").getInt("id"),
                                CategoryModel(prod.getJSONObject("sub_category").getInt("id"),
                                        prod.getJSONObject("sub_category").getString("sub_category_name"),
                                        ""),
                                prod.getJSONObject("user").getString("name"),
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
                        } catch (e: Exception) { }

                        items.add(item)
                    }
                } catch (err: Exception) {
                    Log.e("prod", err.message!!)
                }
            }
        }

        resultsRV.adapter = ItemsAdapter(this, items, isMine = false)
    }
}
