package com.app.okra.extension

import java.util.*
import java.util.regex.Pattern

fun String.isEmailValid(): Boolean {
    val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
    val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.isPasswordValid(): Boolean {
    val expression = ("^(?=.*[0-9])"
            + "(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*[@#$%^&+=])"
            + "(?=\\S+$).{8,40}$")

    val pattern = Pattern.compile(expression)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}


fun isPhoneNumberValid(phoneNumber :String): Boolean {
    return phoneNumber.length==11
}

fun Long.getFormattedDuration(forceShowHours: Boolean = false): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    } else if (forceShowHours) {
        sb.append("0:")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}


fun hexToDecimalConversion(value :String?): String? {
    return value?.let {
        Integer.parseInt(it, 16).toString()
    }
}
fun decimalToHexConversion(value :String?): String? {
    return value?.let {
        Integer.toHexString(it.toInt()).toString()
    }
}