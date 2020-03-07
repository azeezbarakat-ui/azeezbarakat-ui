package com.crazy_iter.eresta

interface APIs {
    companion object {

        val ROOT = "http://www.northpoleagency.com/idea-app/"
        val BASE = ROOT + "api/"

        val REQUIST = BASE + "requests/"
        val NOTIFICATIONS = BASE + "notifications"
        val CATEGORIES = BASE + "categories/"
        val SUBS = BASE + "sub-categories?category_id="
        val SUBS_SUBS = BASE + "sub-sub-categories?category_id="
        val SUBS_SUBS_ADD = BASE + "sub-sub-categories"

        val COMMENTS = BASE + "comments"

        val PRODUCTS = BASE + "products"
        private val PRODUCT_MEDIA = "$PRODUCTS/media/"
        val PRODUCT_PHOTO = PRODUCT_MEDIA + "photo"
        val PRODUCT_GALLERY = PRODUCT_MEDIA + "photo_gallery"
        val PROD_RATE = BASE + "product-ratings"
        val USER_RATE = BASE + "user-ratings"

        val SEARCH = BASE + "search?"
        val REGISTER = BASE + "register3"

        val LOGIN = BASE + "login"
        val social = "$LOGIN/social"
        val USER = BASE + "user/"
        val changepassword = USER + "changepassword"

        private val USERS = BASE + "users/"
        val TOP_USERS = USERS + "top/"
        val MESSAGES = BASE + "messages"
        val PAYMENT = BASE + "payment"
        val ORDERS = BASE + "orders"
        private val PASSWORD = BASE + "password/"
        val P_RESET = PASSWORD + "reset"
        val P_CREATE = PASSWORD + "create"

        private val USER_MEDIA = USER + "media/"
        val USER_PHOTO = USER_MEDIA + "photo"

        val UNITS = BASE + "units/"
        val currencies = BASE + "currencies/"
        val favoriteSellers = BASE + "favorite-sellers"
        val PUF = BASE + "products-users-following"

        private val twoFactory = BASE + "two-factory/"
        val twoEnable = twoFactory + "enabled"
        val twoCreate = twoFactory + "create"

        val GALLERY = BASE + "photo-gallery"

        val registrationConditions = BASE + "registration-conditions"
        val TAGS = BASE + "tags"

    }


}
