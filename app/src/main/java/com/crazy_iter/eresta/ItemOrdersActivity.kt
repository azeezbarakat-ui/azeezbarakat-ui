package com.crazy_iter.eresta

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.crazy_iter.eresta.Adapters.ItemOrdersAdapter
import kotlinx.android.synthetic.main.activity_item_orders.*

class ItemOrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_orders)

        val id = intent.getIntExtra("id", 0)
        val model = StaticsData.getItemByID(id)

        ordersBackIV.setOnClickListener { onBackPressed() }

        ordersRV.setHasFixedSize(true)
        ordersRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        ordersRV.adapter = ItemOrdersAdapter(this, model?.getOrders() ?: ArrayList())

    }
}
