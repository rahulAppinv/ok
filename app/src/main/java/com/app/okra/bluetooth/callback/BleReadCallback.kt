package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.exception.BleException

abstract class BleReadCallback : BleBaseCallback() {
    abstract fun onReadSuccess(data: ByteArray?)
    abstract fun onReadFailure(exception: BleException?)
}