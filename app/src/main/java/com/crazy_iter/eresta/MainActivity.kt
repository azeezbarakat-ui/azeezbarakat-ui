package com.crazy_iter.eresta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.StaticsData.LOCATION_REQ_CODE
import com.google.firebase.crash.FirebaseCrash
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            throw NullPointerException("Null eResta")
        } catch (e: Exception) {
            Log.e("fire", e.toString())
            FirebaseCrash.log(e.toString())
            FirebaseCrash.report(Exception(JSONObject().toString()))
        }

        checkRequestPermission()
        getNotifications()

//        getCategories()
    }

    private fun checkRequestPermission() {
        val permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQ_CODE)
        } else {
            getCategories()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQ_CODE -> {
                StaticsData.getMyLocation(this)
                getCategories()
            }
        }
    }


    private fun getNotifications() {
        if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
            return
        }

        val queue = Volley.newRequestQueue(this)
        val notiReq = object : JsonObjectRequest(APIs.NOTIFICATIONS, null, {
            queue.cancelAll("noti")
            val notis = it.getJSONArray("notifications")
            for (i in 0 until notis.length()) {
                val n = notis.getJSONObject(i)
                if (n.getString("read_at") == "null") {

                    try {
                        when {
                            n.getString("type").contains("SellerNotification", true) -> NewsNotifications
                                    .notify(
                                            this, i,
                                            n.getJSONObject("data")
                                                    .getJSONObject("details")
                                                    .getString("user_name"),
                                            n.getJSONObject("data")
                                                    .getJSONObject("details")
                                                    .getString("content"),
                                            Intent(this, MainApp::class.java)
                                                    .putExtra("user",
                                                            n.getJSONObject("data")
                                                                    .getJSONObject("details")
                                                                    .toString())
                                    )
                            n.getString("type").contains("MessageNotification", true) -> NewsNotifications
                                    .notify(
                                            this, i,
                                            n.getJSONObject("data")
                                                    .getJSONObject("details")
                                                    .getString("greeting"),
                                            n.getJSONObject("data")
                                                    .getJSONObject("details")
                                                    .getString("body"),
                                            Intent(this, ProfileActivity::class.java)
                                                    .putExtra("tab", 1)
                                    )
                            n.getString("type").contains("CommentNotification", true) -> NewsNotifications
                                    .notify(
                                            this, i,
                                            n.getJSONObject("data")
                                                    .getJSONObject("details")
                                                    .getString("user_name"),
                                            n.getJSONObject("data")
                                                    .getJSONObject("details")
                                                    .getString("content"),
                                            Intent(this, CommentsActivity::class.java)
                                                    .putExtra("id", n.getJSONObject("data")
                                                            .getJSONObject("details")
                                                            .getString("offer_id"))
                                                    .putExtra("title", n.getJSONObject("data")
                                                            .getJSONObject("details")
                                                            .getString("offer_name"))
                                    )
                        }
                    } catch (e: Exception) {
                        Log.e("notification", e.toString())
                    }
                }
            }

        }, {
            queue.cancelAll("noti")
            Log.e("noti_error", it.toString())
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@MainActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }

        notiReq.tag = "noti"
        queue.add(notiReq)

    }

    private fun getCategories() {
        val queue = Volley.newRequestQueue(this)
        val categoriesJsonRequest = JsonObjectRequest(APIs.CATEGORIES, null, {
            StaticsData.categories.clear()
            val itCats = it.getJSONArray("categories")
//            StaticsData.categories.add(
//                    CategoryModel(
//                            0,
//                            "All Categories",
//                            null
//                    )
//            )
            for (i in 0 until itCats.length()) {
                val catJSON = itCats.getJSONObject(i)
                StaticsData.categories.add(
                        CategoryModel(
                                catJSON.getInt("id"),
                                catJSON.getString("category_name"),
                                catJSON.getString("photo_link")
                        )
                )
            }
            queue.cancelAll("cats")
            startApp()
        }, {
            Log.e("cats_error", it.toString())
            queue.cancelAll("cats")
            StaticsData.retryFun(this, {
                getCategories()
            }, true)
        })
        categoriesJsonRequest.tag = "cats"
        categoriesJsonRequest.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(categoriesJsonRequest)
    }

    private fun startApp() {
        val first = StaticsData.getShared(this, StaticsData.FIRST)
        if (first.isEmpty()) {
            startActivity(Intent(this, TipsActivity::class.java))
        } else {

            if (intent.getBooleanExtra("reset", false)) {
                startActivity(Intent(this, LoginRegisterActivity::class.java)
                        .putExtra("reset", true)
                        .putExtra("app", true)
                        .putExtra("next", StaticsData.RESTART))
            } else {
                startActivity(Intent(this, MainApp::class.java))
            }
        }
        finish()
    }

}