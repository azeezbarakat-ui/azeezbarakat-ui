package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.Models.ItemModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_choose_payment.*
import org.json.JSONObject
import java.util.*

class DialogChoosePaymentWay(context: Context, private val model: ItemModel, private val paymentMethod: String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_payment)

        choosePaymentBackIV.setOnClickListener {
            dismiss()
        }

        cashTV.setOnClickListener {
            payMethod("cash")
        }

        if (paymentMethod.contains("paypal", true)) {
            paypalIV.setOnClickListener {
                payMethod("paypal")
            }
        } else {
            paypalIV.visibility = View.GONE
        }

    }

    private fun payMethod(paymentMethod: String) {
        val loading = DialogLoading(context)
        loading.show()

        val payJSON = JSONObject()
        payJSON.put("product_id", model.id)
        payJSON.put("payment_method", paymentMethod)

        val queue = Volley.newRequestQueue(context)
        val paypalRequest = object : JsonObjectRequest(Method.POST, APIs.PAYMENT, payJSON, {
            queue.cancelAll("pay")
            Log.e("pay", it.toString())
            loading.dismiss()
            if (paymentMethod == "cash") {
                Toast.makeText(context, "Request sent", Toast.LENGTH_LONG).show()
            } else {
                val link = it.getString("approvalUrl")
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            }
            dismiss()
        }, {
            Log.e("pay", it.toString())
            queue.cancelAll("pay")
            loading.dismiss()
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
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
        paypalRequest.tag = "pay"
        queue.add(paypalRequest)
    }

}