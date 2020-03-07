package com.crazy_iter.eresta.Dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.SearchResultsActivity
import com.crazy_iter.eresta.StaticsData
import com.crazy_iter.eresta.StaticsData.myLocation
import kotlinx.android.synthetic.main.dialog_criteria.*
import java.util.*

class DialogCriteria(activity: Activity) : Dialog(activity) {

    private var subCategoryID = 0
    private var categoryID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_criteria)

        searchTimeCB.setOnCheckedChangeListener { _, isChecked ->
            searchDateTimeLL.visibility = if (isChecked) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        setCategories()
        startDate()
        endDate()

        searchBTN.setOnClickListener {
            val loading = DialogLoading(context)
            loading.show()
            val queue = Volley.newRequestQueue(context)

            Log.e("search", getSearchLink())

            val searchArrayRequest = JsonObjectRequest(getSearchLink(), null, {

                loading.dismiss()
                dismiss()
                context.startActivity(Intent(context, SearchResultsActivity::class.java)
                        .putExtra("products", it.toString()))

            }, {
                loading.hide()
                Log.e("search", it.toString())
                queue.cancelAll("search")
            })
            searchArrayRequest.tag = "search"
            queue.add(searchArrayRequest)
        }

    }

    private fun getSearchLink(): String {
        var link = APIs.SEARCH

        link += "category_id=$categoryID"

        if (searchTV.text.isNotEmpty()) {
            link += "&product_name=${searchTV.text}"
        }
        if (searchPriceTV.text.isNotEmpty()) {
            link += "&product_price=${searchPriceTV.text}"
        }

        if (myLocation != null) {
            link += "&product_latitude=${myLocation?.latitude}&"
            link += "product_longitude=${myLocation?.longitude}"
        }

        if (subCategoryID != 0) {
            link += "&sub_category_id=$subCategoryID"
        }

        if (searchStartTimeTIL.editText!!.text.isNotEmpty()) {
            link += "&starting_date=${searchStartTimeTIL.editText!!.text}"
        }

        if (searchEndTimeTIL.editText!!.text.isNotEmpty()) {
            link += "&end_date=${searchEndTimeTIL.editText!!.text}"
        }

        link += "&radius=${searchSB.progress}"

        return link
    }

    // region date

    private fun startDate() {
        startDateRL.setOnClickListener {
            getDateTimeStart()
        }
    }

    private fun endDate() {
        endDateRL.setOnClickListener {
            getDateTimeEnd()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDateTimeStart() {
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

                        searchStartTimeTIL.editText?.setText("$y/$m/$d - $hourOfDay:$minute")

                    }, mHour, mMinute, true)
            timePickerDialog.show()

        }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun getDateTimeEnd() {
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

                        searchEndTimeTIL.editText?.setText("$y/$m/$d - $hourOfDay:$minute")

                    }, mHour, mMinute, true)
            timePickerDialog.show()

        }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    // endregion

    // region categories

    private fun setCategories() {
        val spinnerItems = ArrayList<String>()
        for (i in 0 until StaticsData.categories.size) {
            spinnerItems.add(StaticsData.categories[i].category_name)
        }
        val adapter = ArrayAdapter(context,
                R.layout.support_simple_spinner_dropdown_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        searchCategoriesSP.adapter = adapter
        searchCategoriesSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("selected", "non")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e("position", "$position")
                categoryID = StaticsData.categories[position].id
                loadSubCategories(categoryID)
            }

        }
    }

    private fun loadSubCategories(id: Int?) {
        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val subsRequest = JsonObjectRequest(APIs.SUBS + id, null, {
            StaticsData.subs.clear()
            val subsJSON = it.getJSONArray("sub_categories")
            for (i in 0 until subsJSON.length()) {
                val subObject = subsJSON.getJSONObject(i)
                StaticsData.subs.add(
                        CategoryModel(
                                subObject.getInt("id"),
                                subObject.getString("sub_category_name"),
                                subObject.getString("photo_link")
                        )
                )
            }
            Log.e("subs error", it.toString())
            queue.cancelAll("subs")
            loading.dismiss()
            setSubCategories()

        }, {
            loading.dismiss()
            Log.e("subs error", it.toString())
            queue.cancelAll("subs")
            StaticsData.retryFun(ownerActivity!!, {
                loadSubCategories(id)
            }, false)
        })
        subsRequest.tag = "subs"
        subsRequest.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(subsRequest)
    }

    private fun setSubCategories() {
        val spinnerItems = ArrayList<String>()
        for (category in StaticsData.subs) {
            spinnerItems.add(category.category_name)
        }
        val adapter = ArrayAdapter(context,
                R.layout.support_simple_spinner_dropdown_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        searchSubCategoriesSP.adapter = adapter
        searchSubCategoriesSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("sub selected", "non")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e("position", "$position")
                subCategoryID = StaticsData.subs[position].id
            }

        }
    }

    // endregion

}