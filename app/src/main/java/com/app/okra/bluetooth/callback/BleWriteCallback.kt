package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.exception.BleException

abstract class BleWriteCallback : BleBaseCallback() {
    abstract fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?)
    abstract fun onWriteFailure(exception: BleException?)
}