package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.exception.BleException

abstract class BleNotifyCallback : BleBaseCallback() {
    abstract fun onNotifySuccess()
    abstract fun onNotifyFailure(exception: BleException?)
    abstract fun onCharacteristicChanged(data: ByteArray?)
}