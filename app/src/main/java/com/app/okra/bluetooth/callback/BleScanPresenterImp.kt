package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.data.BleDevice

interface BleScanPresenterImp {
    fun onScanStarted(success: Boolean)
    fun onScanning(bleDevice: BleDevice?)
}