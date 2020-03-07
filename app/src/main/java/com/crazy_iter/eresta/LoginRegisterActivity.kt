package com.crazy_iter.eresta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Dialogs.DialogResetPassword
import com.crazy_iter.eresta.Dialogs.DialogTerms
import com.crazy_iter.eresta.StaticsData.EMAIL
import com.crazy_iter.eresta.StaticsData.LOGIN
import com.crazy_iter.eresta.StaticsData.PUBLIC_PROFILE
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_login_register.*
import org.json.JSONObject



class LoginRegisterActivity : AppCompatActivity() {

    private fun facebook() {
        loginFacebook.setReadPermissions(listOf(EMAIL, PUBLIC_PROFILE))
        loginFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // App code
                Log.e("facebook", loginResult.accessToken.token)
                loginFacebookReq(loginResult.accessToken.token)
            }

            override fun onCancel() {
                // App code
                Log.e("facebook", "cancel")
            }

            override fun onError(exception: FacebookException) {
                Log.e("facebook", exception.toString())
            }
        })
    }

    private fun loginFacebookReq(token: String) {
        val loading = DialogLoading(this)
        loading.show()
        val loginJSON = JSONObject()
        loginJSON.put("provider", "facebook")
        loginJSON.put("access_token", token)

        val queue = Volley.newRequestQueue(this)
        val loginRequest = JsonObjectRequest(Request.Method.POST, APIs.social, loginJSON, {
            queue.cancelAll("login")

            try {
                StaticsData.saveShared(this, StaticsData.TOKEN, it.getString("access_token"))
                Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
                nextActivity()
            } catch (e: Exception) {
                Toast.makeText(this, "Server error, try again", Toast.LENGTH_SHORT).show()
            }
            loading.dismiss()

        }, {
            Log.e("login", it.toString())
            queue.cancelAll("login")
            loading.dismiss()
        })
        loginRequest.tag = "login"
        queue.add(loginRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()

        setContentView(R.layout.activity_login_register)

        facebook()

        if (!intent.getBooleanExtra("app", false)) {
            try {
                val data = "http:${intent.getStringExtra("link")}"
                Log.e("login", data)
                sendValidationRequest(data)
            } catch (e: Exception) {
                Log.e("login", e.message!!)
            }
        }

        checkRequestPermission()

        loginBTN.setOnClickListener {
            if (checkLogin()) {
                login(loginEmailTIL.editText?.text.toString(),
                        loginPasswordTIL.editText?.text.toString())
            }
        }
        registerBTN.setOnClickListener {
            try {
                if (checkRegister()) {
                    register(registerNameTIL.editText?.text.toString(),
                            registerEmailTIL.editText?.text.toString(),
                            registerPhoneTIL.editText?.text.toString(),
                            registerPasswordTIL.editText?.text.toString())
                }
            } catch (e: Exception) {
                Log.e("reg_error", e.toString())
            }
        }
        forgotPasswordTV.setOnClickListener {
            DialogResetPassword(this).show()
        }

        noAccountLL.setOnClickListener {
            loginCV.visibility = View.GONE
            registerCV.visibility = View.VISIBLE
        }
        haveAccountLL.setOnClickListener {
            loginCV.visibility = View.VISIBLE
            registerCV.visibility = View.GONE
        }

        registerTermsTV.setOnClickListener {
            getTerms()
        }

    }

    private fun getTerms() {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val termsReq = JsonObjectRequest(APIs.registrationConditions, null, {
            queue.cancelAll("terms")
            loading.dismiss()

            try {
                val con = it.getJSONObject("registration")
                DialogTerms(this, con.getString("text")).show()
            } catch (e: Exception) {
                DialogTerms(this, "").show()
            }

        }, {
            queue.cancelAll("terms")
            loading.dismiss()
            Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show()
        })
        termsReq.tag = "terms"
        queue.add(termsReq)
    }

    private fun sendValidationRequest(url: String) {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val req = StringRequest(url, {

            queue.cancelAll("val")
            loading.dismiss()

        }, {
            queue.cancelAll("val")
            loading.dismiss()
            StaticsData.retryFun(this, {
                sendValidationRequest(url)
            }, true)

        })

        req.tag = "val"
        queue.add(req)
    }

    private fun register(name: String, email: String, phone: String, password: String) {
        val loading = DialogLoading(this)
        loading.show()
        val info = JSONObject()
        info.put("name", name)
        info.put("email", email)
        info.put("phone", phone)
        info.put("password", password)
        info.put("c_password", password)
        val i = if (businessRB.isChecked) {
            1
        } else {
            0
        }
        info.put("account_type", "$i")

        Log.e("reg", info.toString())

        val queue = Volley.newRequestQueue(this)
        val registerRequest = JsonObjectRequest(Request.Method.POST, APIs.REGISTER, info, {

            queue.cancelAll("reg")
            Toast.makeText(this, "Verify your account please", Toast.LENGTH_SHORT).show()
            loading.dismiss()

//            DialogValidation(this, intent.getIntExtra("next", 0), phone).show()

        }, {
            Log.e("req", it.toString())
            queue.cancelAll("reg")
            try {
                if (it.networkResponse.statusCode == 401) {
//                    DialogValidation(this, intent.getIntExtra("next", 0), phone).show()
                    Toast.makeText(this, "Verify your account please", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            } catch (err: Exception) {
            }
            loading.dismiss()
        })
        registerRequest.tag = "reg"
        queue.add(registerRequest)
    }

    private fun login(email: String, password: String) {
        val loading = DialogLoading(this)
        loading.show()
        val loginJSON = JSONObject()
        loginJSON.put("username", email)
        loginJSON.put("password", password)

        val queue = Volley.newRequestQueue(this)
        val loginRequest = JsonObjectRequest(Request.Method.POST, APIs.LOGIN, loginJSON, {
            queue.cancelAll("login")
//            Log.d("token", it.getString("access_token"))

            StaticsData.saveShared(this, StaticsData.TOKEN, it.getString("token"))
            Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
            nextActivity()
            loading.dismiss()

        }, {
            try {
                if (it.networkResponse.statusCode == 401) {
//                    DialogValidation(this, intent.getIntExtra("next", 0), email).show()
                    Toast.makeText(this, "Check your input please", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
            Log.e("login", it.toString())
            queue.cancelAll("login")
            loading.dismiss()
        })
        loginRequest.tag = "login"
        queue.add(loginRequest)

    }

    private fun checkLogin(): Boolean {
        if (loginEmailTIL.editText?.text!!.isEmpty()
//                !(Patterns.EMAIL_ADDRESS.matcher(loginEmailTIL.editText?.text!!.toString()).matches()) || (Patterns.PHONE.matcher(loginEmailTIL.editText?.text!!.toString()).matches())
        ) {
            loginEmailTIL.error = "Check your username"
            return false
        }
        loginEmailTIL.error = ""

        if (loginPasswordTIL.editText?.text!!.isEmpty()) {
            loginPasswordTIL.error = "Enter Password"
            return false
        }
        loginPasswordTIL.error = ""

        return true
    }

    private fun checkRegister(): Boolean {
        if (registerNameTIL.editText?.text!!.isEmpty()) {
            registerNameTIL.error = "Enter your name"
            return false
        }
        registerNameTIL.error = ""

        if (!Patterns.EMAIL_ADDRESS.matcher(registerEmailTIL.editText?.text!!.toString()).matches()) {
            registerEmailTIL.error = "Check your email"
            return false
        }
        registerEmailTIL.error = ""

        if (!Patterns.PHONE.matcher(registerPhoneTIL.editText?.text!!.toString()).matches()) {
            registerPhoneTIL.error = "Check your phone"
            return false
        }
        registerPhoneTIL.error = ""

        if (registerPasswordTIL.editText?.text!!.length < 8) {
            registerPasswordTIL.error = "Password have to be 8 characters at least"
            return false
        }
        registerPasswordTIL.error = ""

        return true
    }

    private fun nextActivity() {
        when (intent.getIntExtra("next", 0)) {

            StaticsData.PROFILE -> {
                startActivity(Intent(this, ProfileActivity::class.java).putExtra("from", LOGIN))
                finish()
            }

            StaticsData.SELL -> {
                startActivity(Intent(this,
                        ProfileActivity::class.java
                ).putExtra("from", LOGIN))
                finish()
            }

            StaticsData.RESTART -> {
                startActivity(Intent(this, ProfileActivity::class.java).putExtra("from", LOGIN))
                finish()
            }

            StaticsData.ITEM -> finish()

            else -> {
                startActivity(Intent(this, ProfileActivity::class.java).putExtra("from", LOGIN))
                finish()
            }

        }
    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra("reset", false)) {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
            return
        }
        super.onBackPressed()
    }

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

}
