package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.exception.BleException

abstract class BleRssiCallback : BleBaseCallback() {
    abstract fun onRssiFailure(exception: BleException?)
    abstract fun onRssiSuccess(rssi: Int)
}