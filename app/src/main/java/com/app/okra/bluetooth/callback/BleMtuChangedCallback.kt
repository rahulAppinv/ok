package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.exception.BleException

abstract class BleMtuChangedCallback : BleBaseCallback() {
    abstract fun onSetMTUFailure(exception: BleException?)
    abstract fun onMtuChanged(mtu: Int)
}