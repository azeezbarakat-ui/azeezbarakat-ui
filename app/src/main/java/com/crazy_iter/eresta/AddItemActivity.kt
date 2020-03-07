package com.crazy_iter.eresta

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Models.CategoryModel
import com.crazy_iter.eresta.Models.CurrencyModel
import com.crazy_iter.eresta.Models.ItemModel
import com.crazy_iter.eresta.Models.UnitModel
import com.crazy_iter.eresta.StaticsData.myLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.item_tag.view.*
import net.gotev.uploadservice.MultipartUploadRequest
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddItemActivity : AppCompatActivity(), OnMapReadyCallback, SingleUploadBroadcastReceiver.Delegate {

    private var pos: LatLng? = null
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private var subCategoryID: Int? = null
    private var imageUri: Uri? = null
    var imagesUri: ArrayList<Uri> = ArrayList()
    private var isEdit = false
    private var isRequest = false
    private var model: ItemModel? = null
    private val uploadReceiver = SingleUploadBroadcastReceiver()

    // region upload image
    override fun onResume() {
        super.onResume()
        uploadReceiver.register(this)
    }

    override fun onPause() {
        super.onPause()
        uploadReceiver.unregister(this)
    }

    override fun onProgress(progress: Int) {}
    override fun onProgress(uploadedBytes: Long, totalBytes: Long) {}
    override fun onError(exception: java.lang.Exception?) {
        loading.hide()
        Log.e("photo", exception.toString())
    }

    override fun onCancelled() {}
    override fun onCompleted(uploadId: String?, serverResponseCode: Int, serverResponseBody: ByteArray?) {
        Log.e("photo_c", serverResponseBody.toString())
        if (uploadId == uuid) {
            mainPhotoName = JSONObject(String(serverResponseBody!!)).getString("name")
            uploadMoreImage(0)
        } else {
            photosName.add(JSONObject(String(serverResponseBody!!)).getString("name"))
            uploadMoreImage(pi)
            pi++
        }

        if (isFinishedPhotos()) {
            if (isEdit) {
                Log.e("edit_item", "edit")
                editProduct()
            } else {
                if (subSubID == 0) {
                    setupSubSub()
                } else {
                    uploadProduct()
                }
            }
        }
    }

    var pi = 1
    private fun isFinishedPhotos(): Boolean {
        if (photosName.size < imagesUri.size) {
            return false
        }

        if (mainPhotoName == "") {
            return false
        }

        return true
    }

    // endregion

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        isEdit = intent.getBooleanExtra("edit", false)
        isRequest = intent.getBooleanExtra("req", false)

        if (isEdit) {
//            model = if (intent.getBooleanExtra("isMine", false)) {
//                StaticsData.myItems[intent.getIntExtra("position", 0)]
//            } else {
//                StaticsData.getItemByID(intent.getIntExtra("id", 0))!!
//            }

            model = StaticsData.item

            try {
                setDataToEdit()
            } catch (e: Exception) {

            }
            editBTNsLL.visibility = View.VISIBLE
            newBTNsLL.visibility = View.GONE
        } else {
            editBTNsLL.visibility = View.GONE
            newBTNsLL.visibility = View.VISIBLE
        }

        setCategories()
//        setSubSubCategories()

        loadCurrencies()
        loadUnits()
        startSell()

        if (isRequest) {
            addTitleTV.text = "Request"
            editBTNsLL.visibility = View.GONE
            addMorePhotosBTN.visibility = View.GONE
        }

        setupTags()

    }

    private fun setupTags() {
        val queue = Volley.newRequestQueue(this)
        val loading = DialogLoading(this)
        loading.show()
        val tagsReq = JsonObjectRequest(Request.Method.GET, APIs.TAGS, null, { jsonObject ->
            queue.cancelAll("tags")
            Log.e("tags", jsonObject.toString())

            val tagsJSON = jsonObject.getJSONArray("tags")
            for (i in 0 until tagsJSON.length()) {
                val tag = tagsJSON.getJSONObject(i).getString("name")

                val tagV = ViewTag(this, tag)
                val tagView = tagV.view
                tagView.setOnClickListener {
                    if (!tagV.isSelectedTag) {
                        tagView.tagTV.setBackgroundResource(R.drawable.primary_circle)
                        addToDescription(it.tagTV.text.toString())
//                        tagV.isSelectedTag = true
                    }
                }

                tagsLL.addView(tagView)
            }

            loading.dismiss()

        }, {
            queue.cancelAll("tags")
            Log.e("tags", it.toString())
            loading.dismiss()
            StaticsData.retryFun(this, {
                setupTags()
            }, true)
        })
        tagsReq.tag = "tags"
        queue.add(tagsReq)
    }

    private fun addToDescription(name: String) {
        val desc = newDescriptionTIL.editText?.text.toString() + "\n#" + name
        newDescriptionTIL.editText?.setText(desc)
    }

    var subSubID: Int = 0
    private var subsSubs = ArrayList<String>()
    private fun setSubSubCategories() {

        for (sub in StaticsData.subsSubs) {
            subsSubs.add(sub.category_name)
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, subsSubs)
        subSubCategoryACTV.threshold = 1
        subSubCategoryACTV.setAdapter(adapter)
        subSubCategoryACTV.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                subSubID = 0
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                subSubID = StaticsData.subsSubs[position].id
            }

        }
    }

    // region units & currencies

    private var unitID = 0
    private fun loadUnits() {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val subsRequest = object : JsonObjectRequest(APIs.UNITS, null, {
            StaticsData.units.clear()
            val units = it.getJSONArray("units")
            for (i in 0 until units.length()) {
                val u = units.getJSONObject(i)
                StaticsData.units.add(
                        UnitModel(
                                u.getInt("id"),
                                u.getString("unit_name")
                        )
                )
            }
            Log.e("units", it.toString())
            queue.cancelAll("unit")
            loading.dismiss()
            setupUnits()
        }, {
            loading.dismiss()
            Log.e("units_error", it.toString())
            queue.cancelAll("unit")
            StaticsData.retryFun(this, {
                loadUnits()
            }, true)
        }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@AddItemActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        subsRequest.tag = "unit"
        subsRequest.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(subsRequest)
    }

    lateinit var adapterUnits: ArrayAdapter<String>
    private fun setupUnits() {
        StaticsData.unitsName = ArrayList()
        for (i in 0 until StaticsData.units.size) {
            StaticsData.unitsName.add(StaticsData.units[i].name)
        }
        adapterUnits = ArrayAdapter(this,
                R.layout.support_simple_spinner_dropdown_item, StaticsData.unitsName)
        newUnitsSP.adapter = adapterUnits
        newUnitsSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("selected", "non")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e("position", "$position")
                unitID = StaticsData.units[position].id
            }

        }

        if (isEdit) {
            setEditUnit()
        }
    }

    private var currencyID = 0
    private fun loadCurrencies() {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val subsRequest = object : JsonObjectRequest(APIs.currencies, null, {
            StaticsData.currencies.clear()
            val currency = it.getJSONArray("currency")
            for (i in 0 until currency.length()) {
                val cur = currency.getJSONObject(i)
                StaticsData.currencies.add(
                        CurrencyModel(
                                cur.getInt("id"),
                                cur.getString("iso_code")
                        )
                )
            }
            Log.e("currency", it.toString())
            queue.cancelAll("cur")
            loading.dismiss()
            setupCurrencies()
        }, {
            loading.dismiss()
            Log.e("currency_error", it.toString())
            queue.cancelAll("cur")
            StaticsData.retryFun(this, {
                loadCurrencies()
            }, true)
        }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@AddItemActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        subsRequest.tag = "cur"
        subsRequest.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(subsRequest)
    }

    lateinit var adapterCurrencies: ArrayAdapter<String>
    private fun setupCurrencies() {
        StaticsData.currenciesName = ArrayList()
        for (i in 0 until StaticsData.currencies.size) {
            StaticsData.currenciesName.add(StaticsData.currencies[i].iso_code)
        }
        adapterCurrencies = ArrayAdapter(this,
                R.layout.support_simple_spinner_dropdown_item, StaticsData.currenciesName)
        newCurrencySP.adapter = adapterCurrencies
        newCurrencySP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("selected", "non")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e("position", "$position")
                currencyID = StaticsData.currencies[position].id
            }

        }

        if (isEdit) {
            setEditCurrency()
        }
    }

    // endregion

    // region edit

    private fun setEditUnit() {
        for (i in 0 until StaticsData.units.size) {
            if (StaticsData.units[i].id == unitID) {
                newUnitsSP.setSelection(i)
            }
        }
    }

    private fun setEditCurrency() {
        for (i in 0 until StaticsData.currencies.size) {
            if (StaticsData.currencies[i].id == currencyID) {
                newCurrencySP.setSelection(i)
            }
        }
    }

    var b = false
    var bs = false
    private fun setEditCategory() {
        b = false
        for (i in 0 until StaticsData.subs.size) {
            if (StaticsData.subs[i].id == model?.subCategory?.id) {
                newSubCategorySP.setSelection(i)
                b = true
            }
        }

        bs = false
        if (!b) {
            if (newCategorySP.selectedItemPosition < StaticsData.categories.size - 1) {
                newCategorySP.setSelection(newCategorySP.selectedItemPosition + 1)
                bs = true
            }
        }
    }

    private fun setDataToEdit() {
        newNameTIL.editText?.setText(model?.name)
        newColorTIL.editText?.setText(model?.color)
        newPriceTIL.editText?.setText(model?.price?.toString())
        newStartDateTIL.editText?.setText(model?.starting_date)
        newEndDateTIL.editText?.setText(model?.end_date)
        newAddressTIL.editText?.setText(model?.product_address)
        newWeightTIL.editText?.setText(model?.weight.toString())
        newVolumeTIL.editText?.setText(model?.size.toString())
        newModelTIL.editText?.setText(model?.model.toString())
        newDescriptionTIL.editText?.setText(model?.description)
        pos = LatLng(model?.product_latitude!!, model?.product_longitude!!)
        subCategoryID = model?.subCategory?.id
        unitID = model?.unitID!!
        currencyID = model?.currencyID!!
        try {
            Picasso.get()
                    .load(model?.photo_link!!)
                    .into(addMainIV)
        } catch (e: Exception) {
        }

        subSubID = model?.subSub?.id!!
        subSubCategoryACTV.setText(model?.subSub?.category_name)

        val type = model?.product_type!!
        if (type.contains("request")) {
            isRequest = true
        }

        if (type.contains("product")) {
            productRB.isChecked = true
        } else {
            serviceRB.isChecked = true
        }
//        setEditUnit()
//        setEditCurrency()
//        setEditCategory()
    }

    // endregion

    // region upload

    private lateinit var loading: DialogLoading
    private var mainPhotoName = ""
    private var photosName: ArrayList<String> = ArrayList()

    private fun uploadProduct() {
        val create = JSONObject()
        create.put("product_name", newNameTIL.editText?.text.toString())
        create.put("product_price", newPriceTIL.editText?.text.toString())
        create.put("starting_date", newStartDateTIL.editText?.text.toString())
        create.put("end_date", newEndDateTIL.editText?.text.toString())

        // region extra
        if (newWeightTIL.editText?.text.toString().isNotEmpty()) {
            create.put("product_weight", newWeightTIL.editText?.text.toString() + ".0")
        } else {
            create.put("product_weight", "0")
        }
        if (newColorTIL.editText?.text.toString().isNotEmpty()) {
            create.put("product_color", newColorTIL.editText?.text.toString())
        } else {
            create.put("product_color", "non")
        }
        if (newVolumeTIL.editText?.text.toString().isNotEmpty()) {
            create.put("size", newVolumeTIL.editText?.text.toString() + ".0")
        } else {
            create.put("size", "0")
        }
        if (newModelTIL.editText?.text.toString().isNotEmpty()) {
            create.put("model", newModelTIL.editText?.text.toString())
        } else {
            create.put("model", "non")
        }
        // endregion

        create.put("product_address", newAddressTIL.editText?.text.toString())
        create.put("product_description", newDescriptionTIL.editText?.text.toString())
        try {
            create.put("product_latitude", "${pos?.latitude!!}")
            create.put("product_longitude", "${pos?.longitude!!}")
        } catch (e: Exception) {
            create.put("product_latitude", "0.0")
            create.put("product_longitude", "0.0")
        }
        create.put("category_id", "$catID")
        create.put("sub_category_id", "$subCategoryID")
        create.put("sub_sub_category_id", "$subSubID")
        create.put("currency_id", "$currencyID")
        create.put("unit_id", "$unitID")
        create.put("product_state", "1")
        create.put("publishing_later", "$draft")
        create.put("photo", mainPhotoName)

        create.put("allow_comments", if (allowCommentsCB.isChecked) {
            1
        } else {
            0
        })
        create.put("price_type", if (purchRB.isChecked) {
            "purchase"
        } else {
            "donation"
        })
        if (isRequest) {
            create.put("product_type", if (productRB.isChecked) {
                "product_request"
            } else {
                "service_request"
            })
        } else {
            create.put("product_type", if (productRB.isChecked) {
                "product"
            } else {
                "service"
            })
        }

        val galleryJSON = JSONArray()
        for (i in 0 until photosName.size) {
            galleryJSON.put(photosName[i])
        }
        create.put("photo_gallery", galleryJSON)

        Log.e("upload_item", create.toString())

        val queue = Volley.newRequestQueue(this)
        val loading = DialogLoading(this)
        loading.show()
        val req = object : JsonObjectRequest(Method.POST, APIs.PRODUCTS, create, {

            Log.e("response", it.toString())
            loading.dismiss()
            Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show()

            finish()
            startActivity(Intent(this, MainApp::class.java)
                    .putExtra("mine", !isRequest)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

        }, {
            loading.dismiss()
//            Toast.makeText(this, "Server error, please try again", Toast.LENGTH_SHORT).show()
            Log.e("new", it.toString())

            finish()
            startActivity(Intent(this, MainApp::class.java)
                    .putExtra("mine", !isRequest)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

        }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@AddItemActivity, StaticsData.TOKEN)}"
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

    private fun setupSubSub() {
        val loading = DialogLoading(this)
        loading.show()
        val subSubJSON = JSONObject()
        subSubJSON.put("category_id", "$catID")
        subSubJSON.put("sub_category_id", "$subCategoryID")
        subSubJSON.put("sub_sub_category_name", subSubCategoryACTV.text.toString())

        val queue = Volley.newRequestQueue(this)
        val subSubReq = object : JsonObjectRequest(Method.POST, APIs.SUBS_SUBS_ADD, subSubJSON, {
            queue.cancelAll("subSub")
            loading.dismiss()

            try {
                subSubID = it.getInt("id")
            } catch (e: Exception) {

            }
            uploadProduct()

        }, {
            queue.cancelAll("subSub")
            loading.dismiss()
            StaticsData.retryFun(this, {
                setupSubSub()
            }, true)
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@AddItemActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }
        subSubReq.tag = "subSub"
        queue.add(subSubReq)

    }

    private fun editProduct() {
        val editJSON = JSONObject()
        editJSON.put("user_id", model?.userID)
        editJSON.put("product_name", newNameTIL.editText?.text.toString())
        editJSON.put("product_price", newPriceTIL.editText?.text.toString())
        editJSON.put("starting_date", newStartDateTIL.editText?.text.toString())
        editJSON.put("end_date", newEndDateTIL.editText?.text.toString())

        // region extra
        if (newWeightTIL.editText?.text.toString().isNotEmpty()) {
            editJSON.put("product_weight", newWeightTIL.editText?.text.toString())
        } else {
            editJSON.put("product_weight", "0")
        }
        if (newColorTIL.editText?.text.toString().isNotEmpty()) {
            editJSON.put("product_color", newColorTIL.editText?.text.toString())
        } else {
            editJSON.put("product_color", "non")
        }
        if (newVolumeTIL.editText?.text.toString().isNotEmpty()) {
            editJSON.put("size", newVolumeTIL.editText?.text.toString())
        } else {
            editJSON.put("size", "0")
        }
        if (newModelTIL.editText?.text.toString().isNotEmpty()) {
            editJSON.put("model", newModelTIL.editText?.text.toString())
        } else {
            editJSON.put("model", "non")
        }
        // endregion

        editJSON.put("product_address", newAddressTIL.editText?.text.toString())
        editJSON.put("product_description", newDescriptionTIL.editText?.text.toString())
        editJSON.put("product_latitude", "${pos?.latitude!!}")
        editJSON.put("product_longitude", "${pos?.longitude!!}")
        editJSON.put("sub_category_id", "$subCategoryID")
        editJSON.put("category_id", "$catID")
        editJSON.put("sub_sub_category_id", "$subSubID")
        editJSON.put("currency_id", "$currencyID")
        editJSON.put("unit_id", "$unitID")
        editJSON.put("product_state", "1")
        editJSON.put("publishing_later", "0")

        editJSON.put("allow_comments", if (allowCommentsCB.isChecked) {
            1
        } else {
            0
        })
        editJSON.put("product_type", if (productRB.isChecked) {
            "product"
        } else {
            "service"
        })
        editJSON.put("price_type", if (purchRB.isChecked) {
            "purchase"
        } else {
            "donation"
        })

        if (imageUri != null) {
            editJSON.put("photo", mainPhotoName)
        } else {
            editJSON.put("photo_link", model?.photo_link)
        }

        if (photosName.isNotEmpty()) {
            val galleryJSON = JSONArray()
            for (i in 0 until photosName.size) {
                galleryJSON.put(photosName[i])
            }
            editJSON.put("photo_gallery", galleryJSON)
        } else {
            editJSON.put("photo_gallery_link", model?.photo_gallery_link)
        }
        Log.e("edit_item", editJSON.toString())

        val queue = Volley.newRequestQueue(this)
        val loading = DialogLoading(this)
        loading.show()
        val req = object : JsonObjectRequest(Method.PUT, "${APIs.PRODUCTS}/${model?.id}", editJSON, {

            Log.e("response", it.toString())
            loading.dismiss()
            Toast.makeText(this, "Published!", Toast.LENGTH_SHORT).show()

            finish()
            startActivity(Intent(this, MainApp::class.java)
                    .putExtra("mine", !isRequest)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

        }, {
            loading.dismiss()
            Toast.makeText(this, "Server error, please try again", Toast.LENGTH_SHORT).show()
            Log.e("new", it.toString())
        }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@AddItemActivity, StaticsData.TOKEN)}"
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

    private var uuid = ""
    private fun uploadPhotos() {
        uuid = UUID.randomUUID().toString()
        uploadReceiver.setDelegate(this)
        loading = DialogLoading(this)
        loading.show()
        uploadReceiver.setUploadID(uuid)
        try {
            MultipartUploadRequest(this, uuid, APIs.PRODUCT_PHOTO)
                    .setMethod("POST")
                    .addFileToUpload(getPath(imageUri!!), "photo")
                    .setMaxRetries(3)
                    .addHeader("Authorization", "Bearer ${StaticsData.getShared(this@AddItemActivity, StaticsData.TOKEN)}")
                    .startUpload()
        } catch (e: Exception) {
            loading.dismiss()
            Toast.makeText(this, "Choose an image please", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadMoreImage(i: Int) {
        try {
            val uploadID = UUID.randomUUID().toString()
            uploadReceiver.setDelegate(this)
            uploadReceiver.setUploadID(uploadID)
            MultipartUploadRequest(this, uploadID, APIs.PRODUCT_GALLERY)
                    .setMethod("POST")
                    .addFileToUpload(getPath(imagesUri[i]), "photo_gallery")
                    .setMaxRetries(1)
                    .addHeader("Content-Type", "application/json; charset=UTF-8")
                    .addHeader("Authorization", "Bearer ${StaticsData.getShared(this@AddItemActivity, StaticsData.TOKEN)}")
                    .startUpload()
        } catch (e: Exception) {
        }
    }

    private fun checkData(): Boolean {
        if (newNameTIL.editText?.text.toString().isEmpty()) {
            newNameTIL.error = "Enter item name"
            return false
        }

        if (newPriceTIL.editText?.text.toString().isEmpty()) {
            newPriceTIL.error = "Enter item price"
            return false
        }

        if (isRequest) {
            return true
        }

        if (newStartDateTIL.editText?.text.toString().isEmpty()) {
            newStartDateTIL.error = "Enter item start date"
            return false
        }

        if (newEndDateTIL.editText?.text.toString().isEmpty()) {
            newEndDateTIL.error = "Enter item end date"
            return false
        }

        if (newStartDateTIL.editText?.text.toString() > newEndDateTIL.editText?.text.toString()) {
            Toast.makeText(this, "Start date have to be before end date", Toast.LENGTH_SHORT).show()
            return false
        }

//        if (newWeightTIL.editText?.text.toString().isEmpty()) {
//            newWeightTIL.error = "Enter item weight"
//            return false
//        }
//
//        if (newColorTIL.editText?.text.toString().isEmpty()) {
//            newColorTIL.error = "Enter item color"
//            return false
//        }

        if (newAddressTIL.editText?.text.toString().isEmpty()) {
            newAddressTIL.error = "Enter item address"
            return false
        }

        if (newDescriptionTIL.editText?.text.toString().isEmpty()) {
            newDescriptionTIL.error = "Enter item description"
            return false
        }

        if (pos == null) {
            Toast.makeText(this, "Choose item Location", Toast.LENGTH_SHORT).show()
            return false
        }

        if (imageUri == null) {
            Toast.makeText(this, "Choose item image", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // endregion

    // region categories

    lateinit var adapterCategory: ArrayAdapter<String>
    var catID: Int = 0

    private fun setCategories() {
        StaticsData.categoriesName = ArrayList()
        for (i in 0 until StaticsData.categories.size) {
            StaticsData.categoriesName.add(StaticsData.categories[i].category_name)
        }
        adapterCategory = ArrayAdapter(this,
                R.layout.support_simple_spinner_dropdown_item, StaticsData.categoriesName)
        newCategorySP.adapter = adapterCategory
        newCategorySP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("selected", "non")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e("position", "$position")
                catID = StaticsData.categories[position].id
                loadSubCategories(StaticsData.categories[position].id)
            }

        }
    }

    private fun loadSubCategories(id: Int?) {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
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
            StaticsData.retryFun(this, {
                loadSubCategories(id)
            }, true)
        })
        subsRequest.tag = "subs"
        subsRequest.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(subsRequest)
    }

    lateinit var adapterSubs: ArrayAdapter<String>
    private fun setSubCategories() {
        StaticsData.subsName = ArrayList()
        for (category in StaticsData.subs) {
            StaticsData.subsName.add(category.category_name)
        }
        adapterSubs = ArrayAdapter(this,
                R.layout.support_simple_spinner_dropdown_item, StaticsData.subsName)
        newSubCategorySP.adapter = adapterSubs
        newSubCategorySP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("sub selected", "non")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e("position", "$position")
                subCategoryID = StaticsData.subs[position].id
                loadSubSubs()
            }

        }

        if (isEdit) {
            setEditCategory()
        }
    }

    private fun loadSubSubs() {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val subsRequest = JsonObjectRequest(APIs.SUBS_SUBS
                + catID + "&sub_category_id=" + subCategoryID, null, {
            StaticsData.subsSubs.clear()
            val subsJSON = it.getJSONArray("sub_sub_catygories")
            for (i in 0 until subsJSON.length()) {
                val subObject = subsJSON.getJSONObject(i)

                StaticsData.subsSubs.add(
                        CategoryModel(
                                subObject.getInt("id"),
                                subObject.getString("sub_sub_category_name"),
                                subObject.getString("photo")
                        )
                )

                StaticsData.subsSubsList = StaticsData.subsSubs.distinct()
            }
            loading.hide()
            Log.e("subs error", it.toString())
            setSubSubCategories()
            queue.cancelAll("subs")

        }, {
            loading.hide()
            Log.e("subs error", it.toString())
            queue.cancelAll("subs")
            StaticsData.retryFun(this, {
                loadSubSubs()
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

    // endregion

    // region image

    private fun getMainImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, StaticsData.MAIN_IMAGE_PICK)
    }

    private fun getImages() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, StaticsData.MULTI_IMAGES_PICK)
    }

    private fun checkCameraPermission(getImage: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE), StaticsData.CAMERA_PERMISSION_CODE)
            } else {
                getImage()
            }
        } else {
            getImage()
        }
    }

    private fun getPath(uri: Uri): String {
        var cursor = contentResolver.query(uri, null, null, null, null)
        cursor!!.moveToFirst()
        var documentId = cursor.getString(0)
        documentId = documentId.substring(documentId.lastIndexOf(":") + 1)
        cursor.close()

        cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", arrayOf(documentId), null)
        cursor!!.moveToFirst()
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()

        return path
    }

    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    StaticsData.MAIN_IMAGE_PICK -> {
                        imageUri = data?.data!!
                        try {
                            val bitmap = StaticsData.getResizedBitmap(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
                            startActivityForResult(Intent(this, CropImageActivity::class.java)
                                    .putExtra(StaticsData.IMAGE, StaticsData.bitmapToBytes(bitmap)),
                                    StaticsData.IMAGE_CROP_REQUEST)
                        } catch (e: Exception) {
                        }
                    }

                    StaticsData.IMAGE_CROP_REQUEST -> {
                        imageUri = data?.data!!
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        addMainIV.setImageBitmap(bitmap)
                    }

                    StaticsData.MULTI_IMAGES_PICK -> {
//                        newMorePhotosLL.removeAllViews()
                        newMorePhotosLL.visibility = View.VISIBLE
//                        imagesUri = ArrayList()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            val clipData = data?.clipData
                            if (clipData != null) {
                                for (i in 0 until clipData.itemCount) {
                                    imagesUri.add(clipData.getItemAt(i).uri)
                                    newMorePhotosLL.addView(MorePhotosView(this, clipData.getItemAt(i).uri).view)
                                }
                            } else {
                                imagesUri.add(data?.data!!)
                                newMorePhotosLL.addView(MorePhotosView(this, data.data!!).view)
                            }
                        }

                    }

                }
            }
        } catch (err: Exception) {
            Log.e("image", err.toString())
        }
    }

    // endregion

    // region date time

    private fun startDate() {
        newSDRL.setOnClickListener {
            getDateTimeStart()
        }
    }

    private fun endDate() {
        newEDRL.setOnClickListener {
            getDateTimeEnd()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDateTimeStart() {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->

            val mHour = c.get(Calendar.HOUR_OF_DAY)
            val mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                        newStartDateTIL.editText?.setText("$y-" +
                                "${(m + 1).toString().padStart(2, '0')}-" +
                                "${d.toString().padStart(2, '0')} " +
                                "${hourOfDay.toString().padStart(2, '0')}:" +
                                "${minute.toString().padStart(2, '0')}:00")

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
        val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->

            val mHour = c.get(Calendar.HOUR_OF_DAY)
            val mMinute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(this,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                        newEndDateTIL.editText?.setText("$y-" +
                                "${(m + 1).toString().padStart(2, '0')}-" +
                                "${d.toString().padStart(2, '0')} " +
                                "${hourOfDay.toString().padStart(2, '0')}:" +
                                "${minute.toString().padStart(2, '0')}:00")

                    }, mHour, mMinute, true)
            timePickerDialog.show()

        }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    // endregion

    // region get location

    private fun checkRequestPermission() {
        val permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), StaticsData.LOCATION_REQ_CODE)
        } else {
            StaticsData.getMyLocation(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            StaticsData.LOCATION_REQ_CODE -> {
                StaticsData.getMyLocation(this)
            }
        }
    }
    // endregion

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        if (isEdit) {
            mMap.addMarker(MarkerOptions().position(pos!!))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, StaticsData.ZOOM_VAL))
        } else {
            if (myLocation != null) {
                pos = myLocation
                mMap.addMarker(MarkerOptions().position(pos!!))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, StaticsData.ZOOM_VAL))
            }
        }

        mMap.setOnMapClickListener {
            pos = it
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(pos!!))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, StaticsData.ZOOM_VAL))
            try {
//                val add = StaticsData.getGeoAddress(this, it.latitude, it.longitude)
//                newAddressTIL.editText?.setText(add)
            } catch (e: Exception) {
                Log.e("address", e.toString())
                newAddressTIL.editText?.setText("")
            }

        }

//        newAddressTIL.editText?.setOnEditorActionListener { _, actionId, event ->
//            if (event != null) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH
//                        || actionId == EditorInfo.IME_ACTION_DONE
//                        || event.action == KeyEvent.ACTION_DOWN
//                        || event.action == KeyEvent.KEYCODE_ENTER
//                ) {
//                    val latlng = StaticsData.getGeoLocate(this, newAddressTIL.editText?.text.toString())
//                    Log.e("address", "${latlng?.latitude}, ${latlng?.longitude}")
//                }
//            } else {
//                val latlng = StaticsData.getGeoLocate(this, newAddressTIL.editText?.text.toString())
//                Log.e("address", "${latlng?.latitude}, ${latlng?.longitude}")
//            }
//            true
//        }

    }

    override fun onBackPressed() {
        val backDialog = AlertDialog.Builder(this)
                .setCancelable(true)
                .setPositiveButton("Yes") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("No", null)

        if (isEdit) {
            backDialog.setMessage("Sure to cancel editing?")
        } else {
            backDialog.setMessage("Sure to cancel selling?")
        }

        backDialog.create()
        backDialog.show()
    }

    private fun startSell() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.newMapF) as SupportMapFragment
        mapFragment.getMapAsync(this)
        newBackIV.setOnClickListener { onBackPressed() }
        startDate()
        endDate()
        checkRequestPermission()

        addMainIV.setOnClickListener {
            checkCameraPermission {
                getMainImage()
            }
        }

        addMorePhotosBTN.setOnClickListener {
            checkCameraPermission {
                getImages()
            }
        }

        newBTN.setOnClickListener {
            if (checkData()) {
                draft = 0
                uploadPhotos()
            }
        }

        newDraftBTN.setOnClickListener {
            draft = 1

            if (imageUri != null) {
                uploadPhotos()
            } else {
                if (subSubID == 0) {
                    setupSubSub()
                } else {
                    uploadProduct()
                }
            }
        }

        editCancelBTN.setOnClickListener { onBackPressed() }

        editBTN.setOnClickListener {
            if (checkEditData()) {
                when {
                    imageUri != null -> uploadPhotos()
                    imagesUri.isNotEmpty() -> uploadMoreImage(0)
                    else -> if (isEdit) {
                        editProduct()
                    } else {
                        if (subSubID == 0) {
                            setupSubSub()
                        } else {
                            uploadProduct()
                        }
                    }
                }
            }
        }

    }

    private var draft = 1

    private fun checkEditData(): Boolean {
        if (newNameTIL.editText?.text.toString().isEmpty()) {
            newNameTIL.error = "Enter item name"
            return false
        }

        if (newPriceTIL.editText?.text.toString().isEmpty()) {
            newPriceTIL.error = "Enter item price"
            return false
        }

        if (newStartDateTIL.editText?.text.toString().isEmpty()) {
            newStartDateTIL.error = "Enter item start date"
            return false
        }

        if (newEndDateTIL.editText?.text.toString().isEmpty()) {
            newEndDateTIL.error = "Enter item end date"
            return false
        }

//        if (newWeightTIL.editText?.text.toString().isEmpty()) {
//            newWeightTIL.error = "Enter item weight"
//            return false
//        }
//
//        if (newColorTIL.editText?.text.toString().isEmpty()) {
//            newColorTIL.error = "Enter item color"
//            return false
//        }

        if (newAddressTIL.editText?.text.toString().isEmpty()) {
            newAddressTIL.error = "Enter item address"
            return false
        }

        if (newDescriptionTIL.editText?.text.toString().isEmpty()) {
            newDescriptionTIL.error = "Enter item description"
            return false
        }

        if (pos == null) {
            Toast.makeText(this, "Choose item Location", Toast.LENGTH_SHORT).show()
            return false
        }

//        if (imageUri == null) {
//            Toast.makeText(this, "Choose item image", Toast.LENGTH_SHORT).show()
//            return false
//        }

        return true
    }

}
