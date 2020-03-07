package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_about.*

class DialogAbout(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_about)

        aboutBackIV.setOnClickListener { onBackPressed() }

        aboutIdeaTV.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(StaticsData.IDEA_URL)))
        }

        getTerms()

    }

    private fun getTerms() {
        val loading = DialogLoading(context)
        loading.show()
        val queue = Volley.newRequestQueue(context)
        val termsReq = JsonObjectRequest(APIs.registrationConditions, null, {
            queue.cancelAll("terms")
            loading.dismiss()

            try {
                val con = it.getJSONObject("about")
                aboutTV.text = con.getString("text")
            } catch (e: Exception) {
            }

        }, {
            queue.cancelAll("terms")
            loading.dismiss()
            Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show()
        })
        termsReq.tag = "terms"
        queue.add(termsReq)
    }

}