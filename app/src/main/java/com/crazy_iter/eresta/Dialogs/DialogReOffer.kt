package com.crazy_iter.eresta.Dialogs

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.Models.ItemModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_re_offer.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class DialogReOffer(context: Context, private val model: ItemModel) : Dialog(context) {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_re_offer)

        reOfferBackIV.setOnClickListener {
            onBackPressed()
        }

        reOfferTV.text = "Re-Offer ${model.name}"
        reOfferDateRL.setOnClickListener {
            getDateTime()
        }

        reOfferBTN.setOnClickListener {
            if (reOfferDateTIL.editText?.text!!.isEmpty()) {
                Toast.makeText(context, "Choose new end date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            editProduct()

        }

    }

    @SuppressLint("SetTextI18n")
    private fun getDateTime() {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->

            val mHour = c.get(Calendar.HOUR_OF_DAY)
            val mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                        reOfferDateTIL.editText?.setText("$y-" +
                                "${m.toString().padStart(2, '0')}-" +
                                "${d.toString().padStart(2, '0')} " +
                                "${hourOfDay.toString().padStart(2, '0')}:" +
                                "${minute.toString().padStart(2, '0')}:00")

                    }, mHour, mMinute, true)
            timePickerDialog.show()

        }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private fun editProduct() {
        val editJSON = JSONObject()
        editJSON.put("user_id", model.userID)
        editJSON.put("product_name", model.name)
        editJSON.put("product_price", model.price)
        editJSON.put("starting_date", model.starting_date)
        editJSON.put("end_date", reOfferDateTIL.editText?.text.toString())
        editJSON.put("product_weight", model.weight)
        editJSON.put("product_color", model.color)
        editJSON.put("size", model.size)
        editJSON.put("model", model.model)
        editJSON.put("product_address", model.product_address)
        editJSON.put("product_description", model.description)
        editJSON.put("product_latitude", model.product_latitude)
        editJSON.put("product_longitude", model.product_longitude)
        editJSON.put("sub_category_id", model.subCategory?.id)
        editJSON.put("currency_id", model.currencyID)
        editJSON.put("unit_id", model.unitID)
        editJSON.put("product_state", "1")
        editJSON.put("publishing_later", "0")
        editJSON.put("photo_link", model.photo_link)
        editJSON.put("photo_gallery_link", model.photo_gallery_link)

        val queue = Volley.newRequestQueue(context)
        val loading = DialogLoading(context)
        loading.show()
        val req = object : JsonObjectRequest(Method.PUT, "${APIs.PRODUCTS}/${model.id}", editJSON, {

            Toast.makeText(context, "Edited!", Toast.LENGTH_SHORT).show()
            loading.dismiss()
            dismiss()

        }, {
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
        req.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        req.tag = "new"
        queue.add(req)
    }

}