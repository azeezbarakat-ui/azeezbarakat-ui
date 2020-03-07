package com.crazy_iter.eresta.Models

import java.io.Serializable

class UserModel : Serializable {
    var id: Int = 0
    var favID: Int = 0
    var name: String
    var photo: String
    lateinit var email: String
    var long: Double = 0.0
    var lat: Double = 0.0
    lateinit var address: String
    lateinit var specialties: String

    constructor(id: Int, name: String, photo: String) {
        this.id = id
        this.name = name
        this.photo = photo
        this.long = 0.0
        this.lat = 0.0
        this.address = ""
    }

    constructor(id: Int, name: String, photo: String, email: String) {
        this.id = id
        this.name = name
        this.photo = photo
        this.email = email
        this.long = 0.0
        this.lat = 0.0
        this.address = ""
    }

    constructor(id: Int, name: String, photo: String, email: String, favID: Int) {
        this.id = id
        this.name = name
        this.photo = photo
        this.email = email
        this.favID = favID
        this.long = 0.0
        this.lat = 0.0
        this.address = ""
    }

    constructor(id: Int, name: String, photo: String, email: String, long: Double, lat: Double, address: String) {
        this.id = id
        this.name = name
        this.photo = photo
        this.email = email
        this.favID = 0
        this.long = long
        this.lat = lat
        this.address = if (address == "null") {
            ""
        } else {
            address
        }
    }

    constructor(id: Int, name: String, photo: String, email: String, specialties: String) {
        this.id = id
        this.name = name
        this.photo = photo
        this.email = email
        this.specialties = specialties
    }

}