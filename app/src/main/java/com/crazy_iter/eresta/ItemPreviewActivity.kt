package com.crazy_iter.eresta

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.crazy_iter.eresta.Adapters.SliderPageAdapter
import com.crazy_iter.eresta.Dialogs.DialogFirstMessage
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Dialogs.DialogMakeOrder
import com.crazy_iter.eresta.Dialogs.DialogProfile
import com.crazy_iter.eresta.Models.ItemModel
import com.crazy_iter.eresta.Models.UserModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.volokh.danylo.hashtaghelper.HashTagHelper
import kotlinx.android.synthetic.main.activity_item_preview.*
import org.json.JSONObject
import java.util.*
import kotlin.math.roundToInt

class ItemPreviewActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var model: ItemModel
    private var pagerImagesPosition = 0
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_preview)

        if (intent.getBooleanExtra("myWish", false)) {
            model = StaticsData.myWish
        } else {
            when (intent.getIntExtra("type", 0)) {
                1 -> {
                    model = StaticsData.valid[intent.getIntExtra("position", 0)]
                }
                2 -> {
                    model = StaticsData.drafts[intent.getIntExtra("position", 0)]
                }
                3 -> {
                    model = StaticsData.expire[intent.getIntExtra("position", 0)]
                }
                0 -> {
                    model = if (intent.getBooleanExtra("isMine", false)) {
                        StaticsData.myItems[intent.getIntExtra("position", 0)]
                    } else {
                        StaticsData.getItemByID(intent.getIntExtra("id", 0))!!
                    }
                }
            }
        }

        mapFragment = supportFragmentManager.findFragmentById(R.id.previewMapF) as SupportMapFragment
        mapFragment.getMapAsync(this)

        previewBackIV.setOnClickListener { onBackPressed() }

        previewNameTV.text = model.name
        nameTV.text = model.name

        previewPriceTV.text = model.price.toString()

        try {
            previewStartDateTV.text = model.starting_date.split(" ")[0] + "\n" + model.starting_date.split(" ")[1]
            previewEndDateTV.text = model.end_date.split(" ")[0] + "\n" + model.end_date.split(" ")[1]
        } catch (e: Exception) {

        }
        previewCategoryTV.text = model.category?.category_name + ", " + model.subCategory?.category_name + ", " + model.subSub?.category_name
        previewColorTV.text = model.color
        previewWeightTV.text = model.weight.toString()
        previewVolumeTV.text = model.size.toString()
        previewModelTV.text = model.model.toString()
        previewAddressTV.text = model.product_address

        previewPriceTypeTV.text = model.price_type

        val hashTagHelper = HashTagHelper.Creator.create(resources.getColor(R.color.colorAccent), object : HashTagHelper.OnHashTagClickListener {
            override fun onHashTagClicked(hashTag: String?) {

                startActivity(Intent(this@ItemPreviewActivity, SearchResultsActivity::class.java)
                        .putExtra("tag", "#$hashTag"))

            }

        })
        previewDescriptionTV.text = model.description
        hashTagHelper.handle(previewDescriptionTV)

        previewRB.rating = model.getRating().toFloat()
        previewSellerEmailTV.text = model.email

        setupOrders()
        setupSliderAndMainImage()

        if (model.weight == 0.0) {
//            previewWCLL.visibility = View.GONE
        }

        if (model.size == 0.0) {
//            previewVMLL.visibility = View.GONE
        }

        try {
            if (StaticsData.getShared(this, StaticsData.USER_ID).toInt() == model.userID) {
                myProductBTNs(true)
            } else {
                myProductBTNs(false)
            }
        } catch (er: Exception) {
            myProductBTNs(false)
        }

        previewOrdersFAB.setOnClickListener {
            startActivity(Intent(this, ItemOrdersActivity::class.java)
                    .putExtra("id", model.id))
        }

        previewSendFAB.setOnClickListener {
            if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                startActivity(Intent(this, LoginRegisterActivity::class.java)
                        .putExtra("next", StaticsData.ITEM)
                        .putExtra("app", true))
            } else {
                DialogFirstMessage(this, UserModel(model.userID, model.name, "")).show()
            }
        }

        val currentDate = StaticsData.getCurrentDateTime()
        if (currentDate > model.end_date) {
            previewOrderBTN.setTextColor(resources.getColor(R.color.colorPrimary))
            previewOrderBTN.setBackgroundResource(R.drawable.profile_circle)
        }

        if (model.product_type.contains("request")) {
            previewOrderBTN.text = "accept request"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                titleHeadLL.setBackgroundColor(getColor(R.color.yellow_hide))
            }
        } else if (model.product_type.contains("product")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                titleHeadLL.setBackgroundColor(getColor(R.color.colorAccent_hide))
            }
        }

        previewOrderBTN.setOnClickListener {

            if (model.product_type.contains("request")) {
                if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                    startActivity(Intent(this, LoginRegisterActivity::class.java)
                            .putExtra("next", StaticsData.ITEM)
                            .putExtra("app", true))
                } else {
                    DialogFirstMessage(this, UserModel(model.userID, model.name, "")).show()
                }
                return@setOnClickListener
            }

            if (currentDate > model.end_date) {
                Toast.makeText(this, "The product is expired!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                startActivity(Intent(this, LoginRegisterActivity::class.java)
                        .putExtra("next", StaticsData.ITEM)
                        .putExtra("app", true))
            } else {
                fun getTerms() {
                    val loading = DialogLoading(this)
                    loading.show()
                    val queue = Volley.newRequestQueue(this)
                    val termsReq = JsonObjectRequest(APIs.registrationConditions, null, {
                        queue.cancelAll("terms")
                        loading.dismiss()

                        try {
                            val con = it.getJSONObject("conditions")
                            DialogMakeOrder(this, model, con.getString("text")).show()
                        } catch (e: Exception) {
                        }

                    }, {
                        queue.cancelAll("terms")
                        loading.dismiss()
                        Toast.makeText(this, "Server error, please try again", Toast.LENGTH_SHORT).show()
                    })
                    termsReq.tag = "terms"
                    queue.add(termsReq)
                }
                getTerms()
            }
        }
        previewEditBTN.setOnClickListener {
            StaticsData.item = model
            startActivity(Intent(this, AddItemActivity::class.java)
                    .putExtra("id", model.id)
                    .putExtra("edit", true)
                    .putExtra("isMine", intent.getBooleanExtra("isMine", false))
                    .putExtra("position", intent.getIntExtra("position", 0)))
        }
        previewDeleteBTN.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("Delete Product")
                    .setMessage("sure to delete ${model.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteItem(model.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show()
        }
        previewFavIV.setOnClickListener {
            if (previewFavIV.drawable.constantState == resources.getDrawable(R.drawable.ic_favorite).constantState) {
                removeFromFavSellers()
            } else {
                addToFavSellers()
            }
        }
        previewSellerEmailTV.setOnClickListener {
            if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                Toast.makeText(this, "You have to login", Toast.LENGTH_SHORT).show()
            } else {
                getInfo()
            }
        }
        previewRB.setOnRatingBarChangeListener { ratingBar, rate, fromUser ->
            ratingItem(if (rate > 5) {
                5F
            } else {
                rate
            })
        }

        if (model.allow_comments == 1) {

            if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                Toast.makeText(this, "You have to login", Toast.LENGTH_SHORT).show()
            } else {
                previewCommentsBTN.setOnClickListener {
                    startActivity(Intent(this, CommentsActivity::class.java)
                            .putExtra("id", model.id)
                            .putExtra("title", model.name))
                }
            }

        } else {
            previewCommentsBTN.visibility = View.GONE
        }

        previewMainIV.setOnClickListener {
            startActivity(Intent(this, ImageActivity::class.java)
                    .putExtra("image", model.photo_link)
                    .putExtra("name", model.name))
        }

        Log.e("type", model.product_type)

    }

    private fun setupOrders() {
        Log.e("orders", model.getOrders().size.toString())
    }

    private fun getInfo() {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val userReq = object : JsonObjectRequest(APIs.USER + model.userID, null, {
            queue.cancelAll("user")
            loading.hide()

            try {
                DialogProfile(this, model.userID, it).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Server error, please try again", Toast.LENGTH_SHORT).show()
            }

        }, {
            queue.cancelAll("user")
            loading.hide()
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@ItemPreviewActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }
        userReq.tag = "user"
        queue.add(userReq)
    }

    private fun ratingItem(rate: Float) {
        val loading = DialogLoading(this)
        loading.show()
        val rateJSON = JSONObject()
        rateJSON.put("product_rating", rate.toInt())
        rateJSON.put("product_id", model.id)
        val queue = Volley.newRequestQueue(this)
        val rateReq = object : JsonObjectRequest(Method.POST, APIs.PROD_RATE, rateJSON, {
            queue.cancelAll("rate")
            loading.dismiss()
            model.myLastRate = rate.roundToInt()
        }, {
            Log.e("rate_error", it.toString())
            queue.cancelAll("rate")
            loading.dismiss()
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@ItemPreviewActivity, StaticsData.TOKEN)}"
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
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val favReq = object : JsonObjectRequest(Method.DELETE, "${APIs.favoriteSellers}/${model.userID}", null, {
            queue.cancelAll("fav")
            loading.dismiss()
            previewFavIV.setImageResource(R.drawable.ic_favorite_border)
        }, {
            Log.e("fav_error", it.toString())
            queue.cancelAll("fav")
            loading.dismiss()
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@ItemPreviewActivity, StaticsData.TOKEN)}"
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
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val favJSON = JSONObject()
        favJSON.put("favorite_seller_id", model.userID)
        val favReq = object : JsonObjectRequest(Method.POST, APIs.favoriteSellers, favJSON, {
            queue.cancelAll("fav")
            loading.dismiss()
            previewFavIV.setImageResource(R.drawable.ic_favorite)
        }, {
            Log.e("fav_error", it.toString())
            queue.cancelAll("fav")
            loading.dismiss()
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@ItemPreviewActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }

        favReq.tag = "fav"
        queue.add(favReq)
    }

    private fun deleteItem(id: Int) {
        val loading = DialogLoading(this)
        loading.show()

        val queue = Volley.newRequestQueue(this)
        val deleteRequest = object : JsonObjectRequest(Method.DELETE, APIs.PRODUCTS + "/" + id, null, {

            Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show()
            queue.cancelAll("del")
            loading.dismiss()

            finish()
            startActivity(Intent(this, MainApp::class.java)
                    .putExtra("mine", true)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

        }, {
            Log.e("del", it.toString())
            queue.cancelAll("del")
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
            loading.hide()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@ItemPreviewActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        deleteRequest.tag = "del"
        queue.add(deleteRequest)
    }

    @SuppressLint("RestrictedApi")
    private fun myProductBTNs(isMine: Boolean) {
        if (isMine) {
            previewOrderBTN.visibility = View.GONE
            previewDeleteBTN.visibility = View.VISIBLE
            previewEditBTN.visibility = View.VISIBLE
            previewSendFAB.visibility = View.GONE
            previewOrdersFAB.visibility = View.VISIBLE

            previewSellerLL.visibility = View.GONE

        } else {
            previewOrderBTN.visibility = View.VISIBLE
            previewDeleteBTN.visibility = View.GONE
            previewEditBTN.visibility = View.GONE
            previewSendFAB.visibility = View.VISIBLE
            previewOrdersFAB.visibility = View.GONE

            previewSellerLL.visibility = View.VISIBLE
            previewRB.setIsIndicator(false)
            previewRB.rating = model.myLastRate.toFloat()

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val pos = LatLng(model.product_latitude, model.product_longitude)
        mMap.addMarker(MarkerOptions().position(pos))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, StaticsData.ZOOM_VAL))

    }

    // region Images Slider and Main Image
    private fun setupSliderAndMainImage() {
        try {

            Glide.with(this)
                    .load(model.photo_link)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(previewMainIV)


//            Picasso.get()
//                    .load(model.photo_link)
//                    .into(previewMainIV)
        } catch (err: Exception) {
        }
        init()
        addBottomDots(0)
        val handler = Handler()
        val update = Runnable {
            pagerImagesPosition = if (pagerImagesPosition == model.photo_gallery_link.length()) {
                0
            } else {
                pagerImagesPosition + 1
            }
            previewVP.setCurrentItem(pagerImagesPosition, true)
        }
        Timer().schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 100, 5000)
    }

    private fun init() {
        previewVP.adapter = SliderPageAdapter(this, model.getGalleryArray())
        previewVP.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                addBottomDots(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    @SuppressLint("ResourceAsColor")
    private fun addBottomDots(currentPage: Int) {
        val dots = arrayOfNulls<TextView>(model.photo_gallery_link.length())

        previewDotsLL.removeAllViews()
        for (i in 0 until dots.size) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 30F
            dots[i]?.setTextColor(R.color.colorPrimary)
            previewDotsLL.addView(dots[i])
        }

        if (dots.isNotEmpty())
            dots[currentPage]?.setTextColor(android.R.color.white)
    }
    // endregion

}
