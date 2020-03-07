package com.crazy_iter.eresta

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crazy_iter.eresta.Dialogs.DialogManagePaypal
import com.crazy_iter.eresta.Dialogs.DialogValidation
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_profile.*
import org.json.JSONObject

@SuppressLint("ValidFragment")
class ProfileFragment(private val infoJSON: JSONObject) : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.profileMapF)
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync(this)

        profileEmailTV.text = if (infoJSON.getString("email") == "null") {
            ""
        } else {
            infoJSON.getString("email")
        }
        profileAddressTV.text = if (infoJSON.getString("address_address") == "null") {
            ""
        } else {
            infoJSON.getString("address_address")
        }
        profilePhoneTV.text = if (infoJSON.getString("phone") == "null") {
            ""
        } else {
            infoJSON.getString("phone")
        }

        paypalEmailTV.text = if (infoJSON.getString("email_paypal") == "null") {
            ""
        } else {
            infoJSON.getString("email_paypal")
        }

        try {
            profileRB.rating = infoJSON.get("user_rating") as Float
        } catch (e: Exception) { }

        verifyIV.setOnClickListener {
            DialogValidation(context!!, null, profilePhoneTV.text.toString()).show()
        }

        paypalAccountIV.setOnClickListener {
            val paypalDialog = DialogManagePaypal(context!!, infoJSON, paypalEmailTV.text.toString())
            paypalDialog.show()
            paypalDialog.setOnDismissListener {
                paypalEmailTV.text = paypalDialog.paypalEmail
            }

        }

    }

    override fun onMapReady(gMap: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = gMap!!
        try {
            val pos = LatLng(infoJSON.getDouble("address_latitude"),
                    infoJSON.getDouble("address_longitude"))

            googleMap.addMarker(MarkerOptions().position(pos))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, StaticsData.ZOOM_VAL))
        } catch (err: Exception) {
            Log.e("location", err.toString())
        }

    }

}
