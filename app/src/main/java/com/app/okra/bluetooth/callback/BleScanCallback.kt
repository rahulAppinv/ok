package com.app.okra.bluetooth.callback

import com.app.okra.bluetooth.data.BleDevice

abstract class BleScanCallback : BleScanPresenterImp {
    abstract fun onScanFinished(scanResultList: List<BleDevice?>?)
    open fun onLeScan(bleDevice: BleDevice?) {}
}