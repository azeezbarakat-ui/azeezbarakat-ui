package com.crazy_iter.eresta


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.Models.ItemModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_home_map.*
import org.json.JSONArray


class HomeMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var mainMapRG: RadioGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainMapRG = view.findViewById(R.id.mainMapRG)
        mapView = view.findViewById(R.id.homeMapMP)

        mainMapRG.setOnCheckedChangeListener { _, _ ->
            when (mainMapRG.checkedRadioButtonId) {
                R.id.productsRB -> putItems(0)
                R.id.servicesRB -> putItems(1)
                R.id.requestsRB -> putItems(2)
            }
        }

        if (mapView != null) {
            mapView.onCreate(null)
            mapView.onResume()
            mapView.getMapAsync(this)
        }

    }

    override fun onMapReady(gMap: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = gMap!!
        googleMap.isMyLocationEnabled = true

        if (StaticsData.myLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(StaticsData.myLocation, StaticsData.ZOOM_VAL))
        }
        setItems()

        googleMap.setOnMarkerClickListener {
            if (!requestsRB.isChecked) {
                startActivity(Intent(context, ItemPreviewActivity::class.java)
                        .putExtra("id", markerID?.get(it.position))
                        .putExtra("isMine", false))
            } else {
                startActivity(Intent(context, ItemPreviewActivity::class.java)
                        .putExtra("id", markerID?.get(it.position))
                        .putExtra("isMine", false)

                )
            }
            true
        }

    }

    private fun setItems() {
        val loading = DialogLoading(context!!)
        loading.show()

        val queue = Volley.newRequestQueue(context)
        val productsRequest = object : JsonObjectRequest(APIs.PRODUCTS, null, {

            googleMap.clear()
            StaticsData.items.clear()

            val prodsJSON = it.getJSONArray("products")
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
                            item.category = CategoryModel(prod.getJSONObject("category").getInt("id"),
                                    prod.getJSONObject("category").getString("category_name"),
                                    "")
                        } catch (e: Exception) {
                        }
                        try {
                            item.subSub = CategoryModel(prod.getJSONObject("sub_sub_category").getInt("id"),
                                    prod.getJSONObject("sub_sub_category").getString("sub_sub_category_name"),
                                    "")
                        } catch (e: Exception) {
                        }
                        StaticsData.items.add(item)
                    }
                } catch (err: Exception) {
                    Log.e("prod", prod.getString("product_name") + " - " + err.message!!)
                }
            }

            putItems()
            loading.dismiss()
        }, {
            Log.e("prods", it.toString())
            queue.cancelAll("prod")
            loading.dismiss()
            StaticsData.retryFun(activity as MainApp, {
                setItems()
            }, true)
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

    private fun getMarkerIcon(color: String): BitmapDescriptor {
        val hsv = FloatArray(3)
        Color.colorToHSV(Color.parseColor(color), hsv)
        return BitmapDescriptorFactory.defaultMarker(hsv[0])
    }

    private var markerID: HashMap<LatLng, Int>? = null

    private fun putItems(i: Int = 0) {
        googleMap.clear()
        markerID = HashMap()
        when (i) {
            0 -> {
                for (item in StaticsData.items) {
                    if (item.product_type.contains("product")
                            && !item.product_type.contains("request")) {

                        val m = MarkerOptions()
                                .position(
                                        LatLng(item.product_latitude,
                                                item.product_longitude
                                        )
                                )
                                .title(item.name)
                                .icon(getMarkerIcon("#00A45B"))

                        markerID!![LatLng(item.product_latitude,
                                item.product_longitude
                        )] = item.id
                        googleMap.addMarker(m).showInfoWindow()
                    }
                }
            }
            1 -> {
                for (item in StaticsData.items) {
                    if (item.product_type.contains("service")
                            && !item.product_type.contains("request")) {

                        val m = MarkerOptions()
                                .position(
                                        LatLng(item.product_latitude,
                                                item.product_longitude
                                        )
                                )
                                .title(item.name)
                                .icon(getMarkerIcon("#004066"))
                        markerID!![LatLng(item.product_latitude,
                                item.product_longitude
                        )] = item.id
                        googleMap.addMarker(m).showInfoWindow()
                    }
                }
            }
            2 -> {
                for (item in StaticsData.items) {
                    if (item.product_type.contains("request")) {

                        val m = MarkerOptions()
                                .position(
                                        LatLng(item.product_latitude,
                                                item.product_longitude
                                        )
                                )
                                .title(item.name)
                                .icon(getMarkerIcon("#FFFFC400"))
                        googleMap.addMarker(m).showInfoWindow()
                        markerID!![LatLng(item.product_latitude,
                                item.product_longitude
                        )] = item.id

                    }
                }
            }
        }
    }


}
