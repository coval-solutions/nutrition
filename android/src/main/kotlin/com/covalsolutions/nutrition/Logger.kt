package com.covalsolutions.nutrition

import io.flutter.Log

const val CHANNEL_NAME = "covalsolutions_nutrition"

class Logger {
    companion object {
        fun debug(prefix: String, message: String) {
            Log.d(CHANNEL_NAME, "[$prefix]: message")
        }
    }
}