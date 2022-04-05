package com.app.okra.utils

import android.annotation.SuppressLint
import android.util.Log
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateFormatter {

    const val HH_MM = "hhmm"
    const val HH_MM_AA: String = "hh:mm aa"
    const val DD_MMM_YYYY_EEEE: String = "dd MMMM yyyy, EEEE"
    const val patternWithTime = "yyyy-MM-dd HH:mm:ss"
    const val patternWithoutTime = "yyyyMMdd"
    const val DATE_DD_MM_YYYY = "dd MMM yyyy"
    const val YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss"

    fun Long?.isTodayDate(): Boolean {
        val dateMillis = this ?: 0
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance().timeInMillis
        Log.d("Date::", "isTodayDate: ${formatter.format(Date(dateMillis))}")
        return formatter.format(Date(dateMillis)).equals(formatter.format(calendar))
    }

    fun Long?.daysCountFromToday(dateFormat: String = patternWithoutTime): Int {
        var days = 0
        this?.let {
            val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
            val today = formatter.parse(formatter.format(Date()))
            val receivedTime = formatter.parse(formatter.format(Date(it)))
            days = TimeUnit.DAYS.convert((today?.time ?: 0 - (receivedTime?.time ?: 0)),
                TimeUnit.MILLISECONDS).toInt()
            Log.d("Date::", "Today: $today -> $receivedTime $days")
        }
        return days
    }

    fun convert24HourTo12HourFormat(timeIn24HourFormat: String): String {
        val dateFormat24Hours = SimpleDateFormat(HH_MM, Locale.getDefault()).parse(timeIn24HourFormat)
        return SimpleDateFormat(HH_MM_AA, Locale.getDefault()).format(dateFormat24Hours!!).toString()
    }

    fun Long?.convertLongToFormattedDate(dateFormat: String = "yyyy-MM-dd HH:mm:ss"): String {
        return if (this ?: 0 > 0) {
            val date = Date(this ?: 0L)
            val format = SimpleDateFormat(dateFormat, Locale.getDefault())
            format.format(date)
        } else {
            "-"
        }
    }

    fun Long?.dateTimeDiff(calendarUnit: Int, endDate: Long? = null): Int {
        val toDate = Calendar.getInstance()
        endDate?.let {
            toDate.timeInMillis = it
        }
        val fromDate = Calendar.getInstance().also {
            it.timeInMillis = this ?: 0
        }
        return (toDate[calendarUnit] - fromDate[calendarUnit])
    }

    fun getDaysBetweenCurrentAndDate(time: Long): Long {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
        val millis = Calendar.getInstance().timeInMillis - (format.parse(format.format(date)).time)
        return TimeUnit.DAYS.convert(millis, TimeUnit.MILLISECONDS)
    }

    fun convertDateIntoMili(date: String): Long {
        return try {

            val format = SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS, Locale.getDefault())
            val dateTime: Date = format.parse(date)
            dateTime.time
        } catch (e: java.lang.Exception) {
            0L
        }

    }

    fun formatDate(date: String, dateFormat: String? = null, returnFormat: String): String? {
        var format = "yyyy-MM-dd'T'HH:mm:ss"
        if (!dateFormat.isNullOrBlank()) {
            format = dateFormat
        }
        var df1: DateFormat = SimpleDateFormat(format, Locale.getDefault())
        val date1: Date?
        val date2: String?
        try {
            date1 = df1.parse(date)
            df1 = SimpleDateFormat(returnFormat, Locale.getDefault())
            date2 = df1.format(date1 ?: "")
            return date2
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return "N/A"
    }

    @SuppressLint("SimpleDateFormat")
    fun covertMiliIntoDate(date: String?, format: String? = DATE_DD_MM_YYYY): String {
        return try {
            if (date.isNullOrEmpty()) return ""
            val formatter = SimpleDateFormat(format ?: DATE_DD_MM_YYYY)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date.toDouble().toLong()
            formatter.format(calendar.time).toString()
        } catch (e: Exception) {
            return ""
        }
    }


    fun convertMilieInto(time: String): String {
        val date = covertMiliIntoDate(time, HH_MM_AA)
        var finalDate = ""
        val h_mm_a = SimpleDateFormat("h:mm a")
        val hh_mm_ss = SimpleDateFormat("HHmm")

        try {
            val d1 = h_mm_a.parse(date)
            finalDate = hh_mm_ss.format(d1)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()

        }
        return finalDate
    }
}