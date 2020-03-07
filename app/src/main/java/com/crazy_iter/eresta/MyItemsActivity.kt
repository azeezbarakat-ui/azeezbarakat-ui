package com.crazy_iter.eresta

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.Models.ItemModel
import kotlinx.android.synthetic.main.activity_my_items.*
import org.json.JSONArray

class MyItemsActivity : AppCompatActivity() {

    private lateinit var validFragment: MyItemsFragment
    private lateinit var draftFragment: MyItemsFragment
    private lateinit var expireFragment: MyItemsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_items)

        myItemsBackIV.setOnClickListener { onBackPressed() }

        getInfo()

    }

    fun getInfo(i: Int = 0) {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val userRequest = object : JsonObjectRequest(Method.GET, APIs.USER, null, {

            loading.dismiss()
            setItems(it.getJSONObject("user_info").getJSONArray("products"), i)

        }, {
            queue.cancelAll("user")
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
            loading.dismiss()
            StaticsData.retryFun(this, {
                getInfo()
            }, true)
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@MyItemsActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        userRequest.tag = "user"
        queue.add(userRequest)
    }

    private fun setItems(myItemsJSON: JSONArray, i: Int) {

        val currentDate = StaticsData.getCurrentDateTime()

        Log.e("my offer", myItemsJSON.toString())

        StaticsData.myItems.clear()
        StaticsData.valid.clear()
        StaticsData.expire.clear()
        StaticsData.drafts.clear()

        for (i in 0 until myItemsJSON.length()) {
            val prod = myItemsJSON.getJSONObject(i)
            try {
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
                        StaticsData.getShared(this, StaticsData.USER_ID).toInt(),
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

                StaticsData.myItems.add(item)

                when {
                    item.publishing_later == 1 -> StaticsData.drafts.add(item)
                    currentDate <= item.end_date -> StaticsData.valid.add(item)
                    else -> StaticsData.expire.add(item)
                }
            } catch (err: Exception) {
            }
        }

        val pagerAdapter = ViewPagerAdapter(supportFragmentManager)

        validFragment = MyItemsFragment(this, StaticsData.valid, pType = 1)
        draftFragment = MyItemsFragment(this, StaticsData.drafts, pType = 2)
        expireFragment = MyItemsFragment(this, StaticsData.expire, pType = 3)

        pagerAdapter.addFragment(validFragment, "Valid Offers")
        pagerAdapter.addFragment(draftFragment, "Drafts")
        pagerAdapter.addFragment(expireFragment, "Expired Offers")
        myItemsVP.adapter = pagerAdapter
        myItemsTL.setupWithViewPager(myItemsVP)

        myItemsVP.currentItem = i

    }

}
