package com.crazy_iter.eresta.Dialogs

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.Models.ItemModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_make_order.*
import org.json.JSONObject
import java.util.*

class DialogMakeOrder(context: Context, private val model: ItemModel, private val con: String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_make_order)

        orderBackIV.setOnClickListener {
            dismiss()
        }

        receivedDateRL.setOnClickListener {
            getDate()
        }

        orderBTN.setOnClickListener {
            makeOrder()
        }

        conditionTV.text = con

    }

    @SuppressLint("SetTextI18n")
    private fun getDate() {
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

                        receivedDateTIL.editText?.setText("$y-${m+1}-$d $hourOfDay:$minute:00")

                    }, mHour, mMinute, true)
            timePickerDialog.show()

        }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private fun makeOrder() {

        if (!orderCB.isChecked) {
            Toast.makeText(context, "You should accept terms of purchase", Toast.LENGTH_SHORT).show()
            return
        }

        val date = receivedDateTIL.editText?.text.toString()
        if (date.isEmpty() || !(date >= model.starting_date && date <= model.end_date)
        ) {
            Toast.makeText(context, "Check Received Date", Toast.LENGTH_SHORT).show()
            return
        }

        val loading = DialogLoading(context)
        loading.show()

        val orderJSON = JSONObject()
        orderJSON.put("product_id", model.id)
        orderJSON.put("user_id", StaticsData.getShared(context, StaticsData.USER_ID))
        orderJSON.put("received_date", receivedDateTIL.editText?.text.toString())

        val queue = Volley.newRequestQueue(context)
        val orderReq = object : JsonObjectRequest(Method.POST, APIs.ORDERS, orderJSON, {
            queue.cancelAll("order")
            Log.e("order", it.toString())
            loading.dismiss()
            dismiss()
            Toast.makeText(context, "Order sent Successfully!" ,Toast.LENGTH_SHORT).show()
        }, {
            Log.e("order", it.toString())
            queue.cancelAll("order")
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
        orderReq.tag = "order"
        queue.add(orderReq)

    }

}