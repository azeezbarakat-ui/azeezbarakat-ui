package com.crazy_iter.eresta


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crazy_iter.eresta.Adapters.ItemsAdapter
import com.crazy_iter.eresta.Models.ItemModel
import kotlinx.android.synthetic.main.fragment_my_items.*

@SuppressLint("ValidFragment")
class MyItemsFragment(private val activity: MyItemsActivity,
                      private val items: ArrayList<ItemModel>,
                      private val isDraft: Boolean = false,
                      private val pType: Int) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myItemsRV.setHasFixedSize(true)
        myItemsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        myItemsRV.adapter = ItemsAdapter(context!!, items, isMine = true, isDraft = isDraft, type = pType, myActivity = activity)

        if (items.isNotEmpty()) {
            noMyItemsTV.visibility = View.GONE
        }

    }
}
