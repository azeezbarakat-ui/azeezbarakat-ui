package com.crazy_iter.eresta.Adapters

import android.content.Context
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
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Models.OrderModel
import com.crazy_iter.eresta.R
import com.crazy_iter.eresta.StaticsData
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

class ItemOrdersAdapter(private val context: Context, private val orders: ArrayList<OrderModel>)
    : RecyclerView.Adapter<ItemOrdersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_user_order, p0,false))

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = orders[position]
        val user = model.user!!
        holder.name.text = user.name
        holder.email.text = user.email
        holder.address.text = user.address
        try {
            Picasso.get()
                    .load(user.photo)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.photo)
        } catch (e: Exception) { }

        setupApprove(model.approved, holder)

        holder.done.setOnClickListener {
            Toast.makeText(context, "Already approved", Toast.LENGTH_SHORT).show()
        }

        holder.notApprove.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("This order has already Canceled, Sure to delete this order?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes") { _, _ ->
                        deleteOrder(position, model, holder)
                    }
                    .create()
                    .show()
        }

        holder.approve.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("Sure to approve this order?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes") { _, _ ->
                        approveOrder(1, model, holder)
                    }
                    .create()
                    .show()
        }

        holder.cancel.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("Sure to cancel this order?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes") { _, _ ->
                        approveOrder(0, model, holder)
                    }
                    .create()
                    .show()
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

    private fun setupApprove(approved: Int, holder: ViewHolder) {
        when (approved) {
            1 -> {
                holder.cancel.visibility = View.GONE
                holder.approve.visibility = View.GONE

                holder.notApprove.visibility = View.GONE
                holder.done.visibility = View.VISIBLE
            }
            0 -> {
                holder.cancel.visibility = View.GONE
                holder.approve.visibility = View.GONE

                holder.notApprove.visibility = View.VISIBLE
                holder.done.visibility = View.GONE
            }
            else -> {
                holder.cancel.visibility = View.VISIBLE
                holder.approve.visibility = View.VISIBLE

                holder.notApprove.visibility = View.GONE
                holder.done.visibility = View.GONE
            }
        }

    }

    private fun approveOrder(i: Int, model: OrderModel, holder: ViewHolder) {

        val loading = DialogLoading(context)
        loading.show()

        val orderJSON = JSONObject()
        orderJSON.put("product_id", model.productID)
        orderJSON.put("user_id", model.userID)
        orderJSON.put("received_date", model.receivedDate)
        orderJSON.put("approved", i)

        val queue = Volley.newRequestQueue(context)
        val approveReq = object : JsonObjectRequest(Method.PUT, APIs.ORDERS + "/" + model.id, orderJSON, {
            queue.cancelAll("approve")
            Log.e("approve", it.toString())
            loading.dismiss()

            StaticsData.setApprove(i, model.productID, model.id)
            setupApprove(i, holder)

            Toast.makeText(context, it.getString("message") ,Toast.LENGTH_SHORT).show()
        }, {
            Log.e("approve", it.toString())
            queue.cancelAll("approve")
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
        approveReq.tag = "approve"
        queue.add(approveReq)

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.orderUserCV)

        val name: TextView = itemView.findViewById(R.id.orderUserNameTV)
        val email: TextView = itemView.findViewById(R.id.orderUserEmailTV)
        val address: TextView = itemView.findViewById(R.id.orderUserAddressTV)
        val photo: CircleImageView = itemView.findViewById(R.id.orderUserCIV)

        val cancel: ImageView = itemView.findViewById(R.id.orderUserCancelIV)
        val approve: ImageView = itemView.findViewById(R.id.orderUserApproveIV)

        val done: ImageView = itemView.findViewById(R.id.orderUserDoneIV)
        val notApprove: ImageView = itemView.findViewById(R.id.orderUserNotApproveIV)
    }

}