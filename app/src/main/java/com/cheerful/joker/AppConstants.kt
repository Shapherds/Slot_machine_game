package com.cheerful.joker

import android.util.Base64

enum class AppConstants(val data: String) {
    APP_ID("") {
        override fun getDecData(): String {
            return data
        }
    },
    KEY("8JGQ6AGe7okmpqwMGQicYk") {
        override fun getDecData(): String {
            return data
        }
    },
    URL("") {
        override fun getDecData() : String {
            val decodedString: String
            val decodedBytes: ByteArray = Base64.decode(data, Base64.DEFAULT)
            decodedString = String(decodedBytes)
            return decodedString
        }
    },
    VR("") {
        override fun getDecData() : String {
            val decodedString: String
            val decodedBytes: ByteArray = Base64.decode(data, Base64.DEFAULT)
            decodedString = String(decodedBytes)
            return decodedString
        }
    },
    CONFIG_NAME("") {
        override fun getDecData(): String {
            return data
        }
    };

    abstract fun getDecData(): String
}
