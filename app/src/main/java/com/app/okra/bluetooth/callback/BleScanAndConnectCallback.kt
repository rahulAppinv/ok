package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.data.BleDevice

abstract class BleScanAndConnectCallback : BleGattCallback(), BleScanPresenterImp {
    abstract fun onScanFinished(scanResult: BleDevice?)
    fun onLeScan(bleDevice: BleDevice?) {}
}