package com.crazy_iter.eresta.Adapters

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.APIs
import com.crazy_iter.eresta.Dialogs.DialogChoosePaymentWay
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.ItemPreviewActivity
import com.crazy_iter.eresta.Models.OrderModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ItemMyOrdersAdapter(private val context: Context, private val orders: ArrayList<OrderModel>)
    : RecyclerView.Adapter<ItemMyOrdersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_my_order, p0,false))

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = orders[position]
        try {
            val product = StaticsData.getItemByID(model.productID)!!
            holder.user.text = model.receivedDate
            holder.product.text = product.name
            try {
                Picasso.get()
                        .load(product.photo_link)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(holder.photo)
            } catch (e: Exception) { }

            setupApprove(model.approved, holder)

            holder.card.setOnClickListener {
                context.startActivity(Intent(context, ItemPreviewActivity::class.java)
                        .putExtra("id", product.id))
            }


            holder.noMoney.setOnClickListener {
                Toast.makeText(context, "Sorry your order not approved", Toast.LENGTH_SHORT).show()
            }

            holder.money.setOnClickListener {
                DialogChoosePaymentWay(context, product, model.paymentMethod).show()
            }


            holder.delete.setOnClickListener {
                AlertDialog.Builder(context)
                        .setMessage("Sure to cancel this order?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes") { _, _ ->
                            deleteOrder(position, model, holder)
                        }
                        .create()
                        .show()
            }
        } catch (e: Exception) {
            holder.card.visibility = View.GONE
            Log.e("order item", "delete")
        }

    }

    private fun setupApprove(approved: Int, holder: ViewHolder) {

        holder.money.visibility = View.GONE
        holder.noMoney.visibility = View.GONE
        holder.delete.visibility = View.GONE

        when (approved) {
            1 -> {
                holder.money.visibility = View.VISIBLE
            }
            0 -> {
                holder.noMoney.visibility = View.VISIBLE
                holder.delete.visibility = View.VISIBLE
            }
            else -> {
                holder.delete.visibility = View.VISIBLE
            }
        }

    }

    private fun deleteOrder(i: Int, model: OrderModel, holder: ViewHolder) {

        val loading = DialogLoading(context)
        loading.show()

        val queue = Volley.newRequestQueue(context)
        val approveReq = object : JsonObjectRequest(Method.DELETE, APIs.ORDERS + "/" + model.id, null, {
            queue.cancelAll("del_order")
            Log.e("del_order", it.toString())
            loading.dismiss()
            Toast.makeText(context, it.getString("message") ,Toast.LENGTH_SHORT).show()
            holder.card.visibility = View.GONE
            orders.removeAt(i)

        }, {
            Log.e("del_order", it.toString())
            queue.cancelAll("del_order")
            loading.dismiss()
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(context, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }
        approveReq.tag = "del_order"
        queue.add(approveReq)

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.myOrderCV)

        val user: TextView = itemView.findViewById(R.id.myOrderUserNameTV)
        val product: TextView = itemView.findViewById(R.id.myOrderProductNameTV)
        val photo: CircleImageView = itemView.findViewById(R.id.myOrderCIV)

        val noMoney: ImageView = itemView.findViewById(R.id.myOrderNoMoneyIV)
        val money: ImageView = itemView.findViewById(R.id.myOrderPayIV)
        val delete: ImageView = itemView.findViewById(R.id.myOrderDeleteIV)
    }

}