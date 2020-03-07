package com.crazy_iter.eresta

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Adapters.CategoriesAdapter
import com.crazy_iter.eresta.Dialogs.*
import com.crazy_iter.eresta.Models.UserModel
import com.crazy_iter.eresta.StaticsData.LOCATION_REQ_CODE
import com.crazy_iter.eresta.StaticsData.getMyLocation
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main_app.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONObject

class MainApp: AppCompatActivity(), View.OnClickListener, LocationListener {

    override fun onLocationChanged(location: Location?) {
        StaticsData.myLocation = LatLng(location?.latitude!!, location.longitude)
        if (StaticsData.getShared(this, StaticsData.TOKEN).isNotEmpty()) {
            try {
                getTopUsers()
            } catch (e: Exception) { }
        }
    }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) { }
    override fun onProviderEnabled(p0: String?) { }
    override fun onProviderDisabled(p0: String?) { }

    lateinit var homeFragment: HomeFragment
    lateinit var homeMapFragment: HomeMapFragment

    companion object {
        var isSubs = false
        var isSubsSubs = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_app)

        getLocation()

        topNotification()

        mainSearchLL.setOnClickListener(this)
        homeBTN.setOnClickListener(this)
        profileBTN.setOnClickListener(this)
        sellLL.setOnClickListener(this)
        infoIV.setOnClickListener(this)
        requestLL.setOnClickListener(this)
        listMapIV.setOnClickListener(this)

        checkRequestPermission()

        if (StaticsData.getShared(this, StaticsData.FIRST).isEmpty()) {
            Handler().postDelayed({
                try {
                    StaticsData.saveShared(this, StaticsData.FIRST, "false")
                    PopupDialog(this).show()
                } catch (e: Exception) {
                }
            }, 5000)
        }

        checkHome()

//        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val myIP = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }

    private fun topNotification() {
        val ii = intent.getStringExtra("user") ?: ""

        if (ii.isNotEmpty()) {
            val details = JSONObject(ii)
            val itt = JSONObject()
            val itVal = JSONObject()
            itVal.put("name", details.getString("user_name"))
            itVal.put("photo_link", "")
            itt.put("user_info", itVal)
            DialogProfile(this, details.getInt("user_id"), itt, isTop = true).show()
        }
    }

    private fun checkHome() {
        val map = StaticsData.getShared(this, StaticsData.MAP)
        if (map.isEmpty()) {
            listMapIV.setImageResource(R.drawable.ic_pin_drop)
            setHome()
        } else {
            listMapIV.setImageResource(R.drawable.ic_list)
            setMapHome()
        }
    }

    private fun setMapHome() {
        homeMapFragment = HomeMapFragment()
        val trans = supportFragmentManager.beginTransaction()
        trans.replace(R.id.mainContentFL, homeMapFragment)
        trans.commit()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 100f, this)
    }

    private fun getTopUsers() {
        homeFragment.homeSR?.isRefreshing = true
        val url = APIs.TOP_USERS + "?latitude=${StaticsData.myLocation?.latitude}&longitude=${StaticsData.myLocation?.longitude}"
        Log.e("top", url)
        val queue = Volley.newRequestQueue(this)
        val userRequest = object : JsonObjectRequest(Method.GET, url,null, {
            queue.cancelAll("top")

            val topList = ArrayList<UserModel>()
            val usersJSON = it.getJSONArray("top_users")
            for (i in 0 until usersJSON.length()) {
                val user = usersJSON.getJSONObject(i)
                topList.add(
                        UserModel(
                                user.getInt("id"),
                                user.getString("name"),
                                user.getString("photo_link"),
                                user.getString("email"),
                                user.getString("specialties")
                        )
                )
            }

            try {
                homeFragment.setupTopUsers(topList)
            } catch (e: Exception) {
                Log.e("top_user", e.toString())
                homeSR?.isRefreshing = false
            }

        }, {
            Log.e("top", it.toString())
            queue.cancelAll("top")
            homeSR?.isRefreshing = false
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@MainApp, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        userRequest.tag = "top"
        queue.add(userRequest)
    }

    override fun onBackPressed() {
        if (isSubs) {
            isSubs = false
            homeFragment.categoriesRV.adapter = CategoriesAdapter(this, homeFragment, StaticsData.categories)
            return
        }

        AlertDialog.Builder(this)
                .setMessage("sure to exit?")
                .setPositiveButton("Exit") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("No", null)
                .create()
                .show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mainSearchLL -> DialogCriteria(this).show()
            R.id.homeBTN -> setHome()
            R.id.profileBTN -> {

                Log.e("token", StaticsData.getShared(this, StaticsData.TOKEN))

                if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                    startActivity(Intent(this, LoginRegisterActivity::class.java)
                            .putExtra("next", StaticsData.PROFILE)
                            .putExtra("app", true))
                } else {
                    try {
                        startActivity(Intent(this, ProfileActivity::class.java).putExtra("mine", homeFragment.myRB.isChecked))
                    } catch (e: Exception) {
                        startActivity(Intent(this, ProfileActivity::class.java)
                                .putExtra("mine", false))
                    }
                }
            }
            R.id.sellLL -> {
                if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                    startActivity(Intent(this, LoginRegisterActivity::class.java)
//                            .putExtra("next", StaticsData.SELL)
                            .putExtra("app", true))
                } else {
                    startActivity(Intent(this, AddItemActivity::class.java)
                            .putExtra("req", false)
                    )
                }
            }
            R.id.requestLL -> {
                if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                    startActivity(Intent(this, LoginRegisterActivity::class.java)
//                            .putExtra("next", StaticsData.SELL)
                            .putExtra("app", true))
                } else {
                    startActivity(
                            Intent(this, AddItemActivity::class.java)
                                    .putExtra("req", true)
                    )
                }
            }
            R.id.infoIV -> {
                val itemPopupMenu = PopupMenu(this, infoIV)
                itemPopupMenu.menuInflater.inflate(R.menu.about_menu, itemPopupMenu.menu)
                itemPopupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.aboutMapMI -> {
                            // TODO
                            true
                        }
                        R.id.aboutMI -> {
                            DialogAbout(this).show()
                            true
                        }
                        R.id.contactUsMI -> {
                            DialogContactUs(this).show()
                            true
                        }
                        R.id.countryCurrencyMI -> {
                            if (StaticsData.getShared(this, StaticsData.TOKEN).isEmpty()) {
                                Toast.makeText(this, "Login required!", Toast.LENGTH_SHORT).show()
                                false
                            } else {
                                DialogCountryCurrency(this).show()
                                true
                            }
                        }
                        else -> false
                    }
                }
                itemPopupMenu.show()
            }
            R.id.listMapIV -> {
                val map = StaticsData.getShared(this, StaticsData.MAP)
                if (map.isEmpty()) {
                    StaticsData.saveShared(this, StaticsData.MAP, StaticsData.MAP)
                } else {
                    StaticsData.saveShared(this, StaticsData.MAP, "")
                }

                checkHome()

            }
        }
    }

    fun setHome() {
        homeFragment = HomeFragment(intent.getBooleanExtra("mine", false))
//        getTopUsers()
        val trans = supportFragmentManager.beginTransaction()
        trans.replace(R.id.mainContentFL, homeFragment)
        trans.commit()
        // change icons
        homeBTN.setImageResource(R.drawable.ic_home_colored)
        profileBTN.setImageResource(R.drawable.ic_user_gray)
    }

    // region get location

    private fun checkRequestPermission() {
        val permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQ_CODE)
        } else {
            getMyLocation(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQ_CODE -> {
                getMyLocation(this)
            }
        }
    }

    // endregion

}