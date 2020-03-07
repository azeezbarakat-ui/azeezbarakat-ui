package com.crazy_iter.eresta

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class FromURLActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_from_url)

        try {
            val data = "${intent.data?.schemeSpecificPart}"
            Log.e("data", data)

            finish()
            if (data.contains("password/find", true)) {
                startActivity(Intent(this, ResetPasswordActivity::class.java)
                        .putExtra("link", "http:$data"))
                return
            }

            startActivity(Intent(this, LoginRegisterActivity::class.java)
                    .putExtra("link", data))

        } catch (e: Exception) {
            Log.e("data", e.message!!)
        }

    }
}
