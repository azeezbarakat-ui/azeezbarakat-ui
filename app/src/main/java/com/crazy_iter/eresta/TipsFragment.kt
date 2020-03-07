package com.crazy_iter.eresta


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_tips.*

@SuppressLint("ValidFragment")
class TipsFragment(val i: Int) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tips, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (i) {
            0 -> {
                imageIV.setImageResource(R.drawable.logo)
                textTV.text = "Social Home Restaurants Network"
            }
            1 -> {
                imageIV.setImageResource(R.drawable.logo)
                textTV.text = "Food Sale and Purchase Service"
            }
            2 -> {
                imageIV.setImageResource(R.drawable.logo)
                textTV.text = "Exchange and Donate Food"
            }
        }

    }
}
