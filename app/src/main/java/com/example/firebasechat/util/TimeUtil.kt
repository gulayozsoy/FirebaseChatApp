package com.example.firebasechat.util


import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {

    fun timeStamptoDate(timeStamp: String): String {

        return try {
            // https://developer.android.com/reference/java/text/SimpleDateFormat.html
            val sdf: DateFormat = SimpleDateFormat("HH:mm")
            val netDate = Date(timeStamp.toLong())
            sdf.format(netDate)
        } catch (ex: Exception) {
            "xx"
        }
    }
}
