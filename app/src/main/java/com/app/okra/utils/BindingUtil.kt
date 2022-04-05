package com.app.okra.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("bindDate")
fun bindDate(view: TextView, date: Long?) {
    view.text = ("on ${
        DateFormatter.formatDate(
            date.toString() ?: "",
            returnFormat = "dd MMM yyyy"
        )
    }")
}