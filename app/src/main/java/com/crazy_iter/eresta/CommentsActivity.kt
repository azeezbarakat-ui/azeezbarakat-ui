package com.crazy_iter.eresta

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crazy_iter.eresta.Adapters.CommentsAdapter
import com.crazy_iter.eresta.Dialogs.DialogLoading
import com.crazy_iter.eresta.Models.CommentModel
import com.crazy_iter.eresta.Models.UserModel
import kotlinx.android.synthetic.main.activity_comments.*
import org.json.JSONObject

class CommentsActivity : AppCompatActivity() {

    private var id: Int = 0
    private lateinit var comments: ArrayList<CommentModel>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        commentsBackIV.setOnClickListener { onBackPressed() }

        val title = intent.getStringExtra("title") ?: ""
        id = intent.getIntExtra("id", 0)
        commentsTitleTV.text = "$title comments"

        comments = ArrayList()
        commentsRV.setHasFixedSize(true)
        commentsRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        getComments()
        commentSendIV.setOnClickListener {
            val text = commentET.text.toString()
            if (text.isNotEmpty()) {
                commentET.setText("")
                sendComment(text)
            }
        }

    }

    private fun putComments() {
        if (comments.isNotEmpty()) {
            commentsRV.adapter = CommentsAdapter(this, comments)
            noCommentsTV.visibility = View.GONE
        }
    }

    private fun getComments() {
        val loading = DialogLoading(this)
        loading.show()
        val queue = Volley.newRequestQueue(this)
        val commentReq = object : JsonObjectRequest(Method.GET, APIs.COMMENTS + "?product_id=" + id, null, {
            queue.cancelAll("com")
            loading.dismiss()

            comments.clear()
            val commentsJSON = it.getJSONArray("comments")
            for (i in 0 until commentsJSON.length()) {
                val cJSON = commentsJSON.getJSONObject(i)
                comments.add(
                        CommentModel(
                                cJSON.getInt("id"),
                                cJSON.getString("comment"),
                                UserModel(
                                        cJSON.getJSONObject("user").getInt("id"),
                                        cJSON.getJSONObject("user").getString("name"),
                                        cJSON.getJSONObject("user").getString("photo_link")
                                ),
                                id,
                                cJSON.getString("created_at")
                        )
                )
            }

            putComments()

        }, {
            loading.dismiss()
            queue.cancelAll("com")
            Log.e("com", it.toString())
        })
        {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@CommentsActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }

        commentReq.tag = "com"
        queue.add(commentReq)
    }

    private fun sendComment(text: String) {
        val loading = DialogLoading(this)
        loading.show()

        val queue = Volley.newRequestQueue(this)
        val commentJSON = JSONObject()
        commentJSON.put("user_id", StaticsData.getShared(this, StaticsData.USER_ID))
        commentJSON.put("product_id", id)
        commentJSON.put("comment", text)

        val commentReq = object : JsonObjectRequest(Method.POST, APIs.COMMENTS, commentJSON, {
            queue.cancelAll("add_com")
            loading.dismiss()

            getComments()

        }, {
            loading.dismiss()
            queue.cancelAll("add_com")
            Log.e("add_com", it.toString())
        })
        {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "Bearer ${StaticsData.getShared(this@CommentsActivity, StaticsData.TOKEN)}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

        }

        commentReq.tag = "add_com"
        queue.add(commentReq)

    }
}
