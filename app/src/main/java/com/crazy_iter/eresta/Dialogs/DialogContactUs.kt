package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import kotlinx.android.synthetic.main.dialog_contact_us.*

class DialogContactUs(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_contact_us)

        contactBackIV.setOnClickListener { onBackPressed() }

        contactCallLL.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:${StaticsData.SUPPORT_MOBILE}")))
        }

        contactEmailLL.setOnClickListener {
            val sendEmail = Intent(Intent.ACTION_SEND)
            sendEmail.type = "message/rfc822"
            sendEmail.putExtra(Intent.EXTRA_EMAIL, arrayOf(StaticsData.SUPPORT_EMAIL))
            sendEmail.putExtra(Intent.EXTRA_SUBJECT, "My feedback from ${context.getString(R.string.app_name)}")
            try {
                context.startActivity(Intent.createChooser(sendEmail, "How to send feedback?"))
            } catch (ex: Exception) {}
        }

        contactWebsiteLL.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(StaticsData.APP_URL)))
        }

    }
}