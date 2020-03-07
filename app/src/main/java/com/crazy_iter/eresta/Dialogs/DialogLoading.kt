package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.crazy_iter.eresta.R

class DialogLoading(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)

        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}