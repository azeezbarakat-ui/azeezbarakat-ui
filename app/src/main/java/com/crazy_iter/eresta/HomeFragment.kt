package com.crazy_iter.eresta

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Adapters.CategoriesAdapter
import com.crazy_iter.eresta.Adapters.ItemsAdapter
import com.crazy_iter.eresta.Adapters.TopUsersAdapter
import com.crazy_iter.eresta.Adapters.UsersAdapter
import com.crazy_iter.eresta.Dialogs.DialogCriteria
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.Models.ItemModel
import com.crazy_iter.eresta.Models.UserModel
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

@SuppressLint("ValidFragment")
class HomeFragment(private val isMine: Boolean = false) : Fragment() {

    lateinit var myView: View
    lateinit var homeSR: SwipeRefreshLayout
//    lateinit var loading: DialogLoading
    lateinit var allRB: RadioButton
    lateinit var myRB: RadioButton
    lateinit var prodsRB: RadioButton
    lateinit var mainRG: RadioGroup
    lateinit var mainMapRG: RadioGroup
    lateinit var categoriesRV: RecyclerView
    lateinit var itemsRV: RecyclerView
    lateinit var searchFAB: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_home, container, false)

        findViews()
        setCategories()
        setItems()
        setRBs()

        searchFAB.setOnClickListener {
            DialogCriteria(activity!!).show()
        }
        homeSR.setOnRefreshListener {
            Handler().postDelayed({
                homeSR.isRefreshing = false
            }, 2000)
        }
        return myView
    }

    fun setupTopUsers(topList: ArrayList<UserModel>) {
        mainTopUsersRV.setHasFixedSize(true)
        mainTopUsersRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mainTopUsersRV.adapter = TopUsersAdapter(context!!, topList)
        homeSR.isRefreshing = false
    }

    private fun setRBs() {
        mainRG.setOnCheckedChangeListener { radioGroup, _ ->
            when (radioGroup.checkedRadioButtonId) {
                R.id.allRB -> {
                    mainMapRG.visibility = View.VISIBLE
                    prodsRB.isChecked = true
                    putItems(false)
                }
                R.id.myRB -> {
                    mainMapRG.visibility = View.GONE
                    putItems(true)
                }
                R.id.favRB -> {
                    mainMapRG.visibility = View.GONE
                    getFavSellers()
                }
            }
        }

        mainMapRG.setOnCheckedChangeListener { _, i ->
            val items = ArrayList<ItemModel>()
            when (mainMapRG.checkedRadioButtonId) {
                R.id.productsRB -> {
                    for (item in StaticsData.items) {
                        if (item.product_type.contains("product") && !item.product_type.contains("request")) {
                            items.add(item)
                        }
                    }
                }
                R.id.servicesRB -> {
                    for (item in StaticsData.items) {
                        if (item.product_type.contains("service") && !item.product_type.contains("request")) {
                            items.add(item)
                        }
                    }
                }
                R.id.requestsRB -> {
                    for (item in StaticsData.items) {
                        if (item.product_type.contains("request")) {
                            items.add(item)
                        }
                    }
                }
            }

            itemsRV.adapter = ItemsAdapter(context!!, items)

        }

    }

    private fun setCategories() {
        categoriesRV.setHasFixedSize(true)
        categoriesRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        categoriesRV.adapter = CategoriesAdapter(activity as MainApp, this, StaticsData.categories)
    }

    private fun setItems() {
        val loading = DialogLoading(context!!)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val productsRequest = object : JsonObjectRequest(APIs.PRODUCTS, null, {

            StaticsData.items.clear()
            val prodsJSON = it.getJSONArray("products")
            Log.e("itemJSON", prodsJSON.length().toString())
            for (i in 0 until prodsJSON.length()) {
                val prod = prodsJSON.getJSONObject(i)
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
                            item.price_type = prod.getString("price_type")
                        } catch (e: Exception) {

                        }

                        try {
                            item.category = CategoryModel(prod.getJSONObject("category").getInt("id"),
                                    prod.getJSONObject("category").getString("category_name"),
                                    "")
                            item.subSub = CategoryModel(prod.getJSONObject("sub_sub_category").getInt("id"),
                                    prod.getJSONObject("sub_sub_category").getString("sub_sub_category_name"),
                                    "")
                        } catch (e: Exception) { }

                        StaticsData.items.add(item)
                    }
                } catch (err: Exception) {
                    Log.e("prod", err.message!!)
                }
            }

            val b = try {
                StaticsData.getShared(context!!, StaticsData.TOKEN).isEmpty()
            } catch (e: Exception) { false }
            if (b) {
                mainRG.visibility = View.GONE
                putItems(null)
            } else {
                if (isMine) {
                    myRB.isChecked = true
                    putItems(true)
                } else {
                    putItems(false)
                }
                mainRG.visibility = View.VISIBLE
            }

            loading.dismiss()


        }, {
            loading.dismiss()
            Log.e("prods", it.toString())
            queue.cancelAll("prod")
            StaticsData.retryFun(activity as MainApp, {
                setItems()
            }, false)
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                val token = StaticsData.getShared(context!!, StaticsData.TOKEN)
                if (token.isNotEmpty()) {
                    params["Authorization"] = "Bearer $token"
                }
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }
        productsRequest.tag = "prod"
        queue.add(productsRequest)
    }

    private fun getFavSellers() {
        val loading = DialogLoading(context!!)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val productsRequest = object : JsonObjectRequest(APIs.favoriteSellers + "?user_id=" + StaticsData.getShared(context!!, StaticsData.USER_ID), null, {

            queue.cancelAll("seller")
            val followers = it.getJSONArray("following")
            StaticsData.followers.clear()
            for (i in 0 until followers.length()) {
                val fJSON = followers.getJSONObject(i)
                StaticsData.followers.add(UserModel(
                        fJSON.getInt("favorite_seller_id"),
                        fJSON.getString("name"),
                        fJSON.getString("photo_link"),
                        fJSON.getString("email"),
                        fJSON.getInt("id")
                ))
            }

            StaticsData.followers.distinct()
            itemsRV.setHasFixedSize(true)
            itemsRV.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
            itemsRV.adapter = UsersAdapter(context!!, StaticsData.followers)

            loading.dismiss()

        }, {
            Log.e("seller", it.toString())
            loading.dismiss()
            queue.cancelAll("seller")
            StaticsData.retryFun(activity as MainApp, {
                getFavSellers()
            }, true)
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
        productsRequest.tag = "prod"
        queue.add(productsRequest)
    }

    fun putItems(isMyItems: Boolean?, categoryModel: CategoryModel? = null) {
        itemsRV.setHasFixedSize(true)
        itemsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        var items = ArrayList<ItemModel>()
        when (isMyItems) {
            true -> {
                val id = StaticsData.getShared(context!!, StaticsData.USER_ID).toInt()
                for (item in StaticsData.items) {
                    if (item.userID == id) {
                        items.add(item)
                    }
                }
            }
            false -> {
                try {
                    val id = StaticsData.getShared(context!!, StaticsData.USER_ID).toInt()
                    for (item in StaticsData.items) {
                        if (item.userID != id) {
                            items.add(item)
                        }
                    }
                } catch (e: Exception) {
                    items = StaticsData.items
                }
            }
            null -> {
                mainMapRG.visibility = View.VISIBLE
                prodsRB.isChecked = true
                items = StaticsData.items
            }
        }

        try {
            for (i in 0 until items.size) {
                if (items[i].userID == StaticsData.getShared(context!!, StaticsData.USER_ID).toInt()) {
                    items.removeAt(i)
                }
            }
        } catch (e: Exception) {

        }

        if (categoryModel != null) {
            val finalItems = ArrayList<ItemModel>()
            for (i in items) {
                if (i.subCategory?.id == categoryModel.id) {
                    finalItems.add(i)
                }
            }
            itemsRV.adapter = ItemsAdapter(context!!, finalItems, isMine = isMyItems!!)
        } else {
            itemsRV.adapter = ItemsAdapter(context!!, items)
        }
    }

    private fun findViews() {
//        loading = DialogLoading(context!!)
        searchFAB = myView.findViewById(R.id.homeSearchFAB)
        homeSR = myView.findViewById(R.id.homeSR)
        categoriesRV = myView.findViewById(R.id.categoryRV)
        itemsRV = myView.findViewById(R.id.itemsRV)
        allRB = myView.findViewById(R.id.allRB)
        myRB = myView.findViewById(R.id.myRB)
        mainRG = myView.findViewById(R.id.mainRG)
        mainMapRG = myView.findViewById(R.id.mainMapRG)
        prodsRB = myView.findViewById(R.id.productsRB)
    }

}
