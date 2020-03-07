package com.crazy_iter.eresta.Models

class OrderModel(val id: Int,
                 val userID: Int,
                 val productID: Int,
                 val receivedDate: String,
                 var approved: Int,
                 val paymentMethod: String,
                 val user: UserModel?)