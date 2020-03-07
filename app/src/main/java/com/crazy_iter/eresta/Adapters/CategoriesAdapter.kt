package com.crazy_iter.eresta.Adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.*
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Models.CategoryModel
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CategoriesAdapter(private val activity: MainApp,
                        private val homeFragment: HomeFragment,
                        private val categories: ArrayList<CategoryModel>)
    : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    private val loading = DialogLoading(activity)

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(activity)
                .inflate(R.layout.category_item, p0, false))

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = categories[position]
        holder.categoryTV.text = model.category_name
        if (categories[0].id == 0) {
            holder.categoryLL.setOnClickListener {
                try {
                    loadSubCategories(model.id)
                } catch (e: Exception) {
                    Log.e("cat_error", e.toString())
                }
            }
        } else {
            holder.categoryLL.setOnClickListener {

                if (MainApp.isSubsSubs) {
                    Toast.makeText(activity, "Items ${model.category_name}", Toast.LENGTH_SHORT).show()
                    homeFragment.putItems(homeFragment.myRB.isChecked, model)
                } else {
                    loadSubSubCategories(model.id, model.parentID)
                }
            }
        }

        when {
            model.id == 0 -> {
                try {
                    Picasso.get()
                            .load(model.photo_link)
                            .placeholder(R.drawable.ic_apps)
                            .into(holder.categoryCIV)
                } catch (err: Exception) {}
                holder.categoryLL.setOnClickListener {
                    Toast.makeText(activity, "All Categories", Toast.LENGTH_SHORT).show()
                    MainApp.isSubs = false
                    activity.setHome()
                }
            }
            model.id == -1 -> {
                holder.categoryCIV.setImageResource(R.drawable.ic_keyboard_arrow_left)
                holder.categoryLL.setOnClickListener {
                    MainApp.isSubs = false
                    MainApp.isSubsSubs = false
                    activity.setHome()
                }
            }
            else -> {

                try {
                    Picasso.get()
                            .load(model.photo_link)
                            .placeholder(R.drawable.ic_apps)
                            .into(holder.categoryCIV)
                } catch (err: Exception) { }

            }
        }

    }

    private fun loadSubCategories(id: Int?) {
        MainApp.isSubs = true
        loading.show()
        val queue = Volley.newRequestQueue(activity)
        val subsRequest = JsonObjectRequest(APIs.SUBS + id, null, {
            StaticsData.subs.clear()
            val subsJSON = it.getJSONArray("sub_categories")
            StaticsData.subs.add(
                    CategoryModel(
                            -1,
                            "Back",
                            null
                    )
            )
            for (i in 0 until subsJSON.length()) {
                val subObject = subsJSON.getJSONObject(i)
                val mm = CategoryModel(
                        subObject.getInt("id"),
                        subObject.getString("sub_category_name"),
                        subObject.getString("photo_link")
                )
                if (id != null) {
                    mm.parentID = id
                }
                StaticsData.subs.add(mm)
            }
            loading.hide()
            Log.e("subs error", it.toString())
            queue.cancelAll("subs")
            homeFragment.categoriesRV.adapter = CategoriesAdapter(activity, homeFragment, StaticsData.subs)
            if (StaticsData.subs.size > 1) {
                homeFragment.putItems(homeFragment.myRB.isChecked, StaticsData.subs[1])
            }

        }, {
            loading.hide()
            Log.e("subs error", it.toString())
            queue.cancelAll("subs")
            StaticsData.retryFun(activity, {
                loadSubCategories(id)
            }, false)
        })
        subsRequest.tag = "subs"
        subsRequest.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(subsRequest)
    }

    private fun loadSubSubCategories(id: Int?, subID: Int) {
        MainApp.isSubsSubs = true
        loading.show()
        val queue = Volley.newRequestQueue(activity)
        val subsRequest = JsonObjectRequest(APIs.SUBS_SUBS
                + id + "&sub_category_id=" + subID, null, {
            StaticsData.subsSubs.clear()
            val subsJSON = it.getJSONArray("sub_sub_catygories")
            StaticsData.subsSubs.add(
                    CategoryModel(
                            -1,
                            "Back",
                            null
                    )
            )
            for (i in 0 until subsJSON.length()) {
                val subObject = subsJSON.getJSONObject(i)
                StaticsData.subsSubs.add(
                        CategoryModel(
                                subObject.getInt("id"),
                                subObject.getString("sub_sub_category_name"),
                                subObject.getString("photo")
                        )
                )
            }
            loading.hide()
            Log.e("subs error", it.toString())
            queue.cancelAll("subs")
            homeFragment.categoriesRV.adapter = CategoriesAdapter(activity, homeFragment, StaticsData.subsSubs)
            if (StaticsData.subsSubs.size > 1) {
                homeFragment.putItems(homeFragment.myRB.isChecked, StaticsData.subsSubs[1])
            }

        }, {
            loading.hide()
            Log.e("subs error", it.toString())
            queue.cancelAll("subs")
            StaticsData.retryFun(activity, {
                loadSubSubCategories(id, subID)
            }, false)
        })
        subsRequest.tag = "subs"
        subsRequest.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(subsRequest)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryLL: LinearLayout = itemView.findViewById(R.id.categoryLL)
        val categoryCIV: CircleImageView = itemView.findViewById(R.id.categoryCIV)
        val categoryTV: TextView = itemView.findViewById(R.id.categoryTV)
    }

}