package com.app.okra.bluetooth.utils

import com.app.okra.bluetooth.bluetooth.BleBluetooth
import java.util.*

class BleLruHashMap<K, V>(private val MAX_SIZE: Int) : LinkedHashMap<K, V>(
    Math.ceil(
        MAX_SIZE / 0.75
    ).toInt() + 1, 0.75f, true
) {
    protected override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        if (size > MAX_SIZE && eldest!!.value is BleBluetooth) {
            (eldest.value as BleBluetooth).disconnect()
        }
        return size > MAX_SIZE
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for ((key, value) in entries) {
            sb.append(String.format("%s:%s ", key, value))
        }
        return sb.toString()
    }
}