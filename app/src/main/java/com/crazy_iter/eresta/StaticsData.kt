package com.crazy_iter.eresta

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.os.ConfigurationCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.util.Patterns
import com.crazy_iter.eresta.Models.*
import com.google.android.gms.maps.model.LatLng
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


object StaticsData {

    val SUPPORT_EMAIL = ""
    val SUPPORT_MOBILE = ""
    val IDEA_URL = "https://www.idea.com/"
    val APP_URL = "https://www.idea.com/"
    val CLIENT_ID = 2
    val CLIENT_SECRET = "vUyhp3M7FzYJQJI6rGGQyteQcVR10orKWQXDE7W4"
    val PASSWORD = "password"
    val IMAGE = "image"
    val EMAIL = "email"
    val PUBLIC_PROFILE = "public_profile"

    val PROFILE = 1
    val SELL = 2
    val LOGIN = 3
    val ITEM = 4
    val RESTART = 5

    val ZOOM_VAL = 16F
    val MAIN_IMAGE_PICK = 11
    val CAMERA_PERMISSION_CODE = 12
    val CAMERA_REQUEST = 13
    val LOCATION_REQ_CODE = 14
    val MULTI_IMAGES_PICK = 15
    val IMAGE_CROP_REQUEST = 16

    var myLocation: LatLng? = null
    fun getMyLocation(activity: Activity) {
        val locationManager: LocationManager? = activity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager?
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, mLocationListener)
        } catch (ex: SecurityException) {
//            getMyLocation(activity)
        }
    }

    private var mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            myLocation = LatLng(location.latitude, location.longitude)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    var categories = ArrayList<CategoryModel>()
    var categoriesName = ArrayList<String>()
    var currencies = ArrayList<CurrencyModel>()
    var currenciesName = ArrayList<String>()
    var units = ArrayList<UnitModel>()
    var unitsName = ArrayList<String>()

    var subs = ArrayList<CategoryModel>()
    var subsSubs = ArrayList<CategoryModel>()
    lateinit var subsSubsList: List<CategoryModel>

    var subsName = ArrayList<String>()
    var items = ArrayList<ItemModel>()
    var myItems = ArrayList<ItemModel>()
    var chats = ArrayList<ChatModel>()
    var followers = ArrayList<UserModel>()
    val productsFollowers = ArrayList<ItemModel>()

    val drafts = ArrayList<ItemModel>()
    val valid = ArrayList<ItemModel>()
    val expire = ArrayList<ItemModel>()

    lateinit var myWish: ItemModel
    lateinit var item: ItemModel

    fun getItemByID(id: Int): ItemModel? {
        for (model in items) {
            if (model.id == id) {
                return model
            }
        }
        return null
    }

    fun removeByID(id: Int) {
        for (i in 0 until items.size) {
            if (items[i].id == id) {
                items.removeAt(i)
            }
        }
    }

    fun setApprove(a: Int, itemID: Int, orderID: Int) {
        for (model in items) {
            if (model.id == itemID) {
                for (order in model.getOrders()) {
                    if (order.id == orderID) {
                        order.approved = a
                    }
                }
            }
        }
    }

    fun retryFun(activity: Activity, retry: () -> Unit, finished: Boolean) {
        AlertDialog.Builder(activity)
                .setTitle("Connection Failed")
                .setMessage("Connection failed check you Internet connection and retry")
                .setPositiveButton("Retry") { _, _ ->
                    retry()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    if (finished) {
                        activity.finish()
                    }
                }
                .setCancelable(false)
                .create()
                .show()
    }

    fun imageToString(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun getResizedBitmap(bm: Bitmap, max: Int = 800): Bitmap {
        try {
            var width = bm.width
            var height = bm.height

            Log.v("Pictures", "Width and height are $width--$height")

            when {
                width > height -> {
                    // landscape
                    val ratio = (width / max).toFloat()
                    width = max
                    height = (height / ratio).toInt()
                }
                height > width -> {
                    // portrait
                    val ratio = (height / max).toFloat()
                    height = max
                    width = (width / ratio).toInt()
                }
                else -> {
                    // square
                    height = max
                    width = max
                }
            }
            Log.v("Pictures", "after scaling Width and height are $width--$height")
            return try {
                Bitmap.createScaledBitmap(bm, width, height, true)
            } catch (err: OutOfMemoryError) {
                bm
            }
        } catch (er: Exception) {
            return bm
        }
    }

    private val APP_NAME = "OLX Idea"
    val TOKEN = "token"
    val USER_ID = "userID"
    val FIRST = "first"
    val MAP = "map"

    fun saveShared(context: Context, name: String, value: String) {
        val editor = context.getSharedPreferences(APP_NAME, MODE_PRIVATE).edit()
        editor.putString(name, value)
        editor.apply()

        Log.d("saved", value)
    }

    fun getShared(context: Context, name: String): String {
        val prefs = context.getSharedPreferences(APP_NAME, MODE_PRIVATE)
        val value = prefs.getString(name, "")!!
        Log.d("saved", value)

        return value
    }

    fun clearShared(context: Context) {
        val editor = context.getSharedPreferences(APP_NAME, MODE_PRIVATE).edit()
        editor.remove(TOKEN)
        editor.remove(USER_ID)
        editor.apply()
    }

    fun isEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPhone(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }

    fun getContryName(): String {
        return ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0).displayName
    }

    fun getCurrentDateTime(): String {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)

        return "$mYear-${mMonth + 1}-$mDay $mHour:$mMinute:00"
    }

    fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun bytesToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun bitmapToUri(context: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "product_main_image", null)
        return Uri.parse(path)
    }

    fun getGeoAddress(context: Context, lat: Double, lng: Double): String {
        val geoCoder = Geocoder(context)
        val address = geoCoder.getFromLocation(lat, lng, 1)[0]
        return address.getAddressLine(0) ?: ""
    }

    fun getGeoLocate(context: Context, address: String): LatLng? {
        val geoCoder = Geocoder(context)
        val addressList: List<Address>

        addressList = geoCoder.getFromLocationName(address, 1)
        return if (addressList.isNotEmpty()) {
            LatLng(addressList[0].latitude, addressList[0].longitude)
        } else {
            null
        }

    }



}
