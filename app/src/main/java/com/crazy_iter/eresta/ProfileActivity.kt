package com.crazy_iter.eresta

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Dialogs.DialogChangePassword
import com.crazy_iter.eresta.Dialogs.DialogEditProfile
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Models.OrderModel
import com.crazy_iter.eresta.StaticsData.CAMERA_PERMISSION_CODE
import com.crazy_iter.eresta.StaticsData.CAMERA_REQUEST
import com.crazy_iter.eresta.StaticsData.LOGIN
import com.crazy_iter.eresta.StaticsData.MAIN_IMAGE_PICK
import com.facebook.login.LoginManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import net.gotev.uploadservice.MultipartUploadRequest
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ProfileActivity : AppCompatActivity(), SingleUploadBroadcastReceiver.Delegate {

    private lateinit var loading: DialogLoading
    private lateinit var dialogEditProfile: DialogEditProfile
    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        loading = DialogLoading(this)
        loading.show()
        getInfo()

        profileBackIV.setOnClickListener { onBackPressed() }

        profileLogoutIV.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage("Sure to logout?")
                    .setNegativeButton("cancel", null)
                    .setPositiveButton("logout") { _, _ ->
                        LoginManager.getInstance().logOut()
                        StaticsData.clearShared(this)
                        Toast.makeText(this, "Bye!", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                    .create()
                    .show()
        }

        profileMyItemsBTN.setOnClickListener {
            startActivity(Intent(this, MyItemsActivity::class.java))
        }

        profileMyGalleryBTN.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

    }

    fun checkCameraPermission(getImage: () -> Unit) {
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

    override fun onBackPressed() {
        finish()
        startActivity(Intent(this, MainApp::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun setupOrders(orders: JSONArray): ArrayList<OrderModel> {
        val ordersList = ArrayList<OrderModel>()
        for (i in 0 until orders.length()) {
            val oneOrder = orders.getJSONObject(i)
            ordersList.add(OrderModel(
                    oneOrder.getInt("id"),
                    oneOrder.getInt("user_id"),
                    oneOrder.getInt("product_id"),
                    oneOrder.getString("received_date"),
                    try { oneOrder.getInt("approved") } catch (e: Exception) { -1 },
                    "",
//                    oneOrder.getString("payment_method"), TODO
                    null
            ))
        }
        return ordersList
    }

    private lateinit var profileFragment: ProfileFragment
    private lateinit var chatFragment: ChatsFragment
    private lateinit var orderFragment: MyOrdersFragment
    private lateinit var wishFragment: WishlistFragment

    private fun showData(infoJSON: JSONObject) {

        profileNameTV.text = infoJSON.getString("name")

        if (infoJSON.getString("specialties") == "null") {
            specialtiesTV.text = ""
        } else {
            specialtiesTV.text = infoJSON.getString("specialties")
        }
        // region profile pager

        profileFragment = ProfileFragment(infoJSON)
        chatFragment = ChatsFragment()
        orderFragment = MyOrdersFragment(setupOrders(infoJSON.getJSONArray("orders")))
        wishFragment = WishlistFragment(infoJSON.getJSONArray("products"))

        val profileVPA = ViewPagerAdapter(supportFragmentManager)
        profileVPA.addFragment(profileFragment, "Profile")
        profileVPA.addFragment(chatFragment, "Chats")
        profileVPA.addFragment(orderFragment, "My Orders")
        profileVPA.addFragment(wishFragment, "Requests")
        profileVP.adapter = profileVPA
        profileTL.setupWithViewPager(profileVP)

        profileVP.currentItem = intent.getIntExtra("tab", 0)

        // endregion

        loading.hide()

        profileCIV.setLayerType(View.LAYER_TYPE_HARDWARE, null)

//        profileCIV.setOnClickListener {
//            DialogChoosePhoto(this).show()
//            getMainImage()
//            checkCameraPermission()
//        }

        profileEditBTN.setOnClickListener {
            dialogEditProfile = DialogEditProfile(this, infoJSON)
            dialogEditProfile.show()

            dialogEditProfile.setOnDismissListener {
                if (DialogEditProfile.isEdit) {
                    showData(infoJSON)
                }
            }

        }

        profileChangePasswordBTN.setOnClickListener {
            DialogChangePassword(this, infoJSON.getString("email")).show()
        }

    }

    lateinit var infoJSON: JSONObject

    private fun getInfo() {
        val queue = Volley.newRequestQueue(this)
        val userRequest = object : JsonObjectRequest(Method.GET, APIs.USER, null, {

            infoJSON = it.getJSONObject("user_info")
            StaticsData.saveShared(this, StaticsData.USER_ID, infoJSON.getInt("id").toString())
            profileNameTV.text = infoJSON.getString("name")
            specialtiesTV.text = infoJSON.getString("specialties")
            try {
                Picasso.get()
                        .load(infoJSON.getString("photo_link"))
                        .placeholder(R.drawable.ic_user_colored)
                        .into(profileCIV)
            } catch (err: Exception) {
            }
            Log.e("photo_user", infoJSON.getString("photo_link"))

            try {
                if (infoJSON.getInt("account_type") != 0) {
                    profileChangePasswordBTN.visibility = View.GONE
                }
            } catch (e: Exception) {

            }

            queue.cancelAll("user")

            if (intent.getIntExtra("from", 0) == LOGIN) {
                startActivity(Intent(this, MainApp::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            } else {
                showData(infoJSON)
            }

        }, {
            Log.e("user", it.toString())
            queue.cancelAll("user")
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
            loading.hide()
            finish()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@ProfileActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        userRequest.tag = "user"
        queue.add(userRequest)
    }

    // region profile image

    fun getMainImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, MAIN_IMAGE_PICK)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE), CAMERA_PERMISSION_CODE)
        } else {
            getMainImage()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
    }

    private lateinit var imageUri: Uri
    private fun takeNewImage() {
        getMainImage()
//        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(pictureIntent, CAMERA_REQUEST)
//        openCameraIntent()
//        val values = ContentValues()
//        values.put(MediaStore.Images.Media.TITLE, "Main Image")
//        values.put(MediaStore.Images.Media.DESCRIPTION, "from Camera")
//        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
//        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                .putExtra(MediaStore.EXTRA_OUTPUT, imageUri), CAMERA_REQUEST)
    }

    private fun openCameraIntent() {
        val pictureIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(packageManager) != null) {
            val imageFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            if (imageFile != null) {
                val photoURI = FileProvider.getUriForFile(this, "com.example.android.provider", imageFile)
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(pictureIntent, CAMERA_REQUEST)
            }
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

    private val uploadReceiver = SingleUploadBroadcastReceiver()
    override fun onResume() {
        super.onResume()
        uploadReceiver.register(this)
    }

    override fun onPause() {
        super.onPause()
        uploadReceiver.unregister(this)
    }

    override fun onProgress(progress: Int) {
        //your implementation
    }

    override fun onProgress(uploadedBytes: Long, totalBytes: Long) {
        //your implementation
    }

    override fun onError(exception: Exception) {
        Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
        loading.hide()
    }

    override fun onCancelled() {
        loading.hide()
        Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
    }

    override fun onCompleted(uploadId: String, serverResponseCode: Int, serverResponseBody: ByteArray) {
        Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show()
        loading.hide()
        editRequest(JSONObject(String(serverResponseBody)).getString("name"))
    }

    private fun editRequest(photo: String) {
        val loading = DialogLoading(this)
        loading.show()

        val editJSON = JSONObject()
        editJSON.put("name", infoJSON.getString("name"))
        editJSON.put("email", infoJSON.getString("email"))
        editJSON.put("photo", photo)
        editJSON.put("account_type", 0)

        Log.e("user", editJSON.toString())

        val queue = Volley.newRequestQueue(this)
        val editRequest = object : JsonObjectRequest(Method.PUT, APIs.USER + infoJSON.getInt("id"), editJSON, {

            Log.e("photo", it.toString())
            loading.dismiss()

        }, {
            Log.e("edit", it.toString())
            queue.cancelAll("edit")
            loading.hide()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@ProfileActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }
        editRequest.tag = "edit"
        queue.add(editRequest)
    }

    private fun setMainImage(image: Bitmap) {
        loading.show()
        bitmap = image

        profileCIV.setImageBitmap(image)

        try {
            dialogEditProfile.image.setImageBitmap(image)
        } catch (e: Exception) {

        }

        val uploadID = UUID.randomUUID().toString()
        uploadReceiver.setDelegate(this)
        uploadReceiver.setUploadID(uploadID)
        MultipartUploadRequest(this, uploadID, APIs.USER_PHOTO)
                .setMethod("POST")
                .addFileToUpload(getPath(imageUri), "photo")
                .setMaxRetries(1)
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .addHeader("Authorization", "Bearer ${StaticsData.getShared(this, StaticsData.TOKEN)}")
                .startUpload()
    }

    // endregion

    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    MAIN_IMAGE_PICK -> {
                        imageUri = data?.data!!
                        setMainImage(StaticsData.getResizedBitmap(
                                BitmapFactory
                                        .decodeStream(contentResolver
                                                .openInputStream(data.data!!)),
                                800
                        ))
                    }

                    CAMERA_REQUEST -> {
                        imageUri = data?.data!!
                        setMainImage(StaticsData
                                .getResizedBitmap(MediaStore.Images.Media.getBitmap(contentResolver,
                                        imageUri),
                                        800))
                    }
                }
            }
        } catch (err: Exception) {
            Log.e("image", err.toString())
        }
    }

}
