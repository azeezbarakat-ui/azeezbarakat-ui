package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import com.crazy_iter.eresta.ProfileActivity
import com.crazy_iter.eresta.R
import kotlinx.android.synthetic.main.dialog_choose_image.*

class DialogChoosePhoto(var activity: ProfileActivity): Dialog(activity) {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_image)

        addImageTakeLL.setOnClickListener {
            dismiss()
            activity.checkCameraPermission()
        }

        addImageSelectLL.setOnClickListener {
            dismiss()
            activity.getMainImage()
        }

        chooseImageBackIV.setOnClickListener {
            dismiss()
        }

    }
}