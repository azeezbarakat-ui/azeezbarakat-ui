package com.crazy_iter.eresta

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crazy_iter.eresta.Adapters.ItemMyOrdersAdapter
import com.crazy_iter.eresta.Models.OrderModel
import kotlinx.android.synthetic.main.fragment_my_orders.*
import java.util.*

@SuppressLint("ValidFragment")
class MyOrdersFragment(val orders: ArrayList<OrderModel>) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myOrdersRV.setHasFixedSize(true)
        myOrdersRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        myOrdersRV.adapter = ItemMyOrdersAdapter(context!!, orders)

    }

}
