package com.crazy_iter.eresta.Models

class CommentModel(val id: Int, val comment: String,
                   val user: UserModel,
                   val productID: Int,
                   val date: String)