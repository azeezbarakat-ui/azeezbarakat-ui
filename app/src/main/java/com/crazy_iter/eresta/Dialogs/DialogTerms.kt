package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.crazy_iter.eresta.R
import kotlinx.android.synthetic.main.dialog_terms.*

class DialogTerms(context: Context, private val terms: String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_terms)

        termsBackIV.setOnClickListener { onBackPressed() }
        termsTV.text = terms

    }

}