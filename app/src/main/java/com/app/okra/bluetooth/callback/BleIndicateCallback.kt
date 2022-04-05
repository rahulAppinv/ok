package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.exception.BleException

abstract class BleIndicateCallback : BleBaseCallback() {
    abstract fun onIndicateSuccess()
    abstract fun onIndicateFailure(exception: BleException?)
    abstract fun onCharacteristicChanged(data: ByteArray?)
}