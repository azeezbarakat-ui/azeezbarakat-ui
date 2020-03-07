package com.crazy_iter.eresta.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.crazy_iter.eresta.R;

public class PopupDialog extends Dialog {

    public PopupDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_popup);
    }
}
