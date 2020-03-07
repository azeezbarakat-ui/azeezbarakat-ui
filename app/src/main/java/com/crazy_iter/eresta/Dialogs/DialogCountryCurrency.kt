package com.crazy_iter.eresta.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.crazy_iter.eresta.Adapters.SpinnerAdapter
import com.crazy_iter.eresta.Models.CountryModel
import com.crazy_iter.eresta.R
import kotlinx.android.synthetic.main.dialog_country_currency.*

class DialogCountryCurrency(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_country_currency)

        ccBackIV.setOnClickListener { onBackPressed() }


        // region countries
        val models = ArrayList<CountryModel>()
        for (i in 0 until 5) {
            models.add(CountryModel(i, "Title $i", ""))
        }
        ccCountrySP.adapter = SpinnerAdapter(context, models)
        ccCountrySP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { }

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                Toast.makeText(context, models[position].title, Toast.LENGTH_SHORT).show()
            }

        }
        // endregion

    }
}