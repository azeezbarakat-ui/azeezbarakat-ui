package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.ProfileActivity
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.dialog_edit_profile.*
import org.json.JSONObject

class DialogEditProfile(private val activity: ProfileActivity, var infoJSON: JSONObject)
    : Dialog(activity), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var pos: LatLng? = null
    lateinit var image: CircleImageView

    companion object {
        var isEdit = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit_profile)

        isEdit = false
        editNameTIL.editText?.setText(infoJSON.getString("name"))
        editSpecialtiesTIL.editText?.setText(infoJSON.getString("specialties"))
        editPhoneTIL.editText?.setText(infoJSON.getString("phone"))
        editEmailTIL.editText?.setText(infoJSON.getString("email"))
        editAddressTIL.editText?.setText(infoJSON.getString("address_address"))

        image = findViewById(R.id.profileDialogCIV)
        image.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        try {
            val bitmap = activity.bitmap
            if (bitmap != null) {
                image.setImageBitmap(bitmap)
            } else {

                Picasso.get()
                        .load(infoJSON.getString("photo_link"))
                        .placeholder(R.drawable.ic_user_colored)
                        .into(image)
            }
        } catch (err: Exception) {
            Log.e("photo", err.message ?: "")
        }

        image.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.checkCameraPermission()
            }
        }

        try {
            pos = LatLng(infoJSON.getDouble("address_latitude"),
                    infoJSON.getDouble("address_longitude"))
        } catch (err: Exception) {
        }

        editBTN.setOnClickListener {
            if (checkData()) {
                editRequest()
            }
        }

        editMV.onCreate(null)
        editMV.onResume()
        editMV.getMapAsync(this)
    }

    private fun editRequest() {
        val loading = DialogLoading(context)
        loading.show()

        val editJSON = JSONObject()
        editJSON.put("id", infoJSON.getInt("id"))
        editJSON.put("name", editNameTIL.editText?.text.toString())
        editJSON.put("email", editEmailTIL.editText?.text.toString())
        editJSON.put("phone", editPhoneTIL.editText?.text.toString())
        editJSON.put("address_address", editAddressTIL.editText?.text.toString())
        editJSON.put("address_latitude", pos?.latitude)
        editJSON.put("address_longitude", pos?.longitude)
        editJSON.put("specialties", editSpecialtiesTIL.editText?.text.toString())
        editJSON.put("account_type", if (businessRB.isChecked) { 1 } else { 0 })

//        val ph = infoJSON.getString("photo_link").split("/")
//        editJSON.put("photo_link", ph[ph.size - 1])

        Log.e("edit", editJSON.toString())

        val queue = Volley.newRequestQueue(context)
        val editRequest = object : JsonObjectRequest(Method.PUT, APIs.USER + infoJSON.getInt("id"), editJSON, {

            Log.e("edit", it.toString())
            Toast.makeText(context, "Edited!", Toast.LENGTH_SHORT).show()
            isEdit = true
            loading.dismiss()
            dismiss()

        }, {
            Log.e("edit", it.toString())
            queue.cancelAll("edit")
            loading.hide()
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
        editRequest.tag = "edit"
        queue.add(editRequest)
    }

    override fun onMapReady(gMap: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = gMap!!

        setOnMap()

        googleMap.setOnMapClickListener {
            pos = it
            setOnMap()
        }

    }

    private fun setOnMap() {
        googleMap.clear()
        try {
            googleMap.addMarker(MarkerOptions().position(pos!!))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, StaticsData.ZOOM_VAL))
        } catch (err: Exception) {
            Log.e("location", err.toString())
        }
    }

    private fun checkData(): Boolean {
        if (editNameTIL.editText?.text?.isEmpty()!!) {
            editNameTIL.error = "Enter your name"
            return false
        }

//        if (StaticsData.isPhone(editPhoneTIL.editText?.text.toString())) {
//            editPhoneTIL.error = "Check your phone"
//            return false
//        }

        if (editAddressTIL.editText?.text!!.isEmpty()) {
            editAddressTIL.error = "Enter your address"
            return false
        }

        if (pos == null) {
            Toast.makeText(context, "Choose your location on map", Toast.LENGTH_SHORT).show()
            return false
        }

        infoJSON.put("name", editNameTIL.editText?.text.toString())
        infoJSON.put("phone", editPhoneTIL.editText?.text.toString())
        infoJSON.put("address_address", editAddressTIL.editText?.text.toString())
        infoJSON.put("address_latitude", pos?.latitude)
        infoJSON.put("address_longitude", pos?.longitude)

        return true
    }

}