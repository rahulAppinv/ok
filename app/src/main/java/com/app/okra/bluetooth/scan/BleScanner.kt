package com.app.okra.bluetooth.scan

import android.annotation.TargetApi
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.callback.BleScanAndConnectCallback
import com.app.okra.bluetooth.callback.BleScanCallback
import com.app.okra.bluetooth.callback.BleScanPresenterImp
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.bluetooth.data.BleScanState
import com.app.okra.bluetooth.utils.BleLog
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleScanner() {
    private object BleScannerHolder {
        val instance = BleScanner()
    }

    var scanState = BleScanState.STATE_IDLE
        private set
    private val mBleScanPresenter: BleScanPresenter = object : BleScanPresenter() {
        override fun onScanStarted(success: Boolean) {
            val callback = this.bleScanPresenterImp
            callback?.onScanStarted(success)
        }

        override fun onLeScan(bleDevice: BleDevice?) {
            if (ismNeedConnect()) {
                val callback: BleScanAndConnectCallback? =
                    this.bleScanPresenterImp as BleScanAndConnectCallback
                callback?.onLeScan(bleDevice)
            } else {
                val callback: BleScanCallback? = this.bleScanPresenterImp as BleScanCallback
                callback?.onLeScan(bleDevice)
            }
        }

        override fun onScanning(result: BleDevice?) {
            val callback = this.bleScanPresenterImp
            callback?.onScanning(result)
        }

        override fun onScanFinished(bleDeviceList: List<BleDevice>?) {
            if (ismNeedConnect()) {
                val callback: BleScanAndConnectCallback? =
                    this.bleScanPresenterImp as BleScanAndConnectCallback
                if (bleDeviceList == null || bleDeviceList.size < 1) {
                    callback?.onScanFinished(null)
                } else {
                    callback?.onScanFinished(bleDeviceList.get(0))
                    val list = bleDeviceList
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        BleManager.instance.connect(
                            list[0], callback
                        )
                    }, 100)
                }
            } else {
                val callback: BleScanCallback = this.bleScanPresenterImp as BleScanCallback
                callback.onScanFinished(bleDeviceList)
            }
        }
    }

    fun scan(serviceUuids: Array<UUID>?,
             names: Array<String>?,
             mac: String?,
             fuzzy: Boolean?,
             timeOut: Long?,
    callback: BleScanCallback?
    ) {
        startLeScan(serviceUuids, names, mac, fuzzy, false, timeOut, callback)
    }

    fun scanAndConnect(
        serviceUuids: Array<UUID>, names: Array<String>, mac: String, fuzzy: Boolean,
        timeOut: Long, callback: BleScanAndConnectCallback?
    ) {
        startLeScan(serviceUuids, names, mac, fuzzy, true, timeOut, callback)
    }

    @Synchronized
    private fun startLeScan(
        serviceUuids: Array<UUID>?,
        names: Array<String>?,
        mac: String?,
        fuzzy: Boolean?,
        needConnect: Boolean?,
        timeOut: Long?
        , imp: BleScanPresenterImp?
    ) {
        if (scanState != BleScanState.STATE_IDLE) {
            BleLog.w("scan action already exists, complete the previous scan action first")
            imp?.onScanStarted(false)
            return
        }
        mBleScanPresenter.prepare(names, mac, fuzzy, needConnect, timeOut, imp)
        val success: Boolean = BleManager.instance.bluetoothAdapter!!
            .startLeScan(serviceUuids, mBleScanPresenter)
        scanState = if (success) BleScanState.STATE_SCANNING else BleScanState.STATE_IDLE
        mBleScanPresenter.notifyScanStarted(success)
    }

    @Synchronized
    fun stopLeScan() {
        BleManager.instance.bluetoothAdapter!!.stopLeScan(mBleScanPresenter)
        scanState = BleScanState.STATE_IDLE
        mBleScanPresenter.notifyScanStopped()
    }

    companion object{
        val instance = BleScannerHolder.instance

    }
}