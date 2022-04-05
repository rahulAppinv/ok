package com.app.okra.bluetooth.scan

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.os.*
import android.text.TextUtils
import com.app.okra.bluetooth.callback.BleScanPresenterImp
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.bluetooth.data.BleMsg
import com.app.okra.bluetooth.utils.BleLog
import com.app.okra.bluetooth.utils.HexUtil
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
abstract class BleScanPresenter : LeScanCallback {
    private var mDeviceNames: Array<String>? = null
    private var mDeviceMac: String? = null
    private var mFuzzy = false
    private var mNeedConnect = false
    private var mScanTimeout: Long = 0
    var bleScanPresenterImp: BleScanPresenterImp? = null
        private set
    private val mBleDeviceList: MutableList<BleDevice> = ArrayList()
    private val mMainHandler = Handler(Looper.getMainLooper())
    private var mHandlerThread: HandlerThread? = null
    private var mHandler: Handler? = null
    private var mHandling = false

    private class ScanHandler internal constructor(
        looper: Looper?,
        bleScanPresenter: BleScanPresenter
    ) : Handler(
        looper!!
    ) {
        private val mBleScanPresenter: WeakReference<BleScanPresenter>
        override fun handleMessage(msg: Message) {
            val bleScanPresenter = mBleScanPresenter.get()
            if (bleScanPresenter != null) {
                if (msg.what == BleMsg.MSG_SCAN_DEVICE) {
                    val bleDevice = msg.obj as BleDevice
                    bleScanPresenter.handleResult(bleDevice)
                }
            }
        }

        init {
            mBleScanPresenter = WeakReference(bleScanPresenter)
        }
    }

    private fun handleResult(bleDevice: BleDevice) {
        mMainHandler.post { onLeScan(bleDevice) }
        checkDevice(bleDevice)
    }

    fun prepare(
        names: Array<String>?, mac: String?, fuzzy: Boolean?, needConnect: Boolean?,
        timeOut: Long?, bleScanPresenterImp: BleScanPresenterImp?
    ) {
        mDeviceNames = names
        mDeviceMac = mac

        fuzzy?.let {
            mFuzzy = it
        }

        needConnect?.let {
            mNeedConnect = needConnect
        }
        timeOut?.let {
            mScanTimeout = it
        }
        this.bleScanPresenterImp = bleScanPresenterImp
        mHandlerThread = HandlerThread(BleScanPresenter::class.java.simpleName)
        mHandlerThread!!.start()
        mHandler = ScanHandler(mHandlerThread!!.looper, this)
        mHandling = true
    }

    fun ismNeedConnect(): Boolean {
        return mNeedConnect
    }

    override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
        if (!mHandling) return
        val message = mHandler!!.obtainMessage()
        message.what = BleMsg.MSG_SCAN_DEVICE
        message.obj = BleDevice(device, rssi, scanRecord, System.currentTimeMillis())
        mHandler!!.sendMessage(message)
    }

    private fun checkDevice(bleDevice: BleDevice) {
        if (TextUtils.isEmpty(mDeviceMac) && (mDeviceNames == null || mDeviceNames!!.size < 1)) {
            correctDeviceAndNextStep(bleDevice)
            return
        }
        if (!TextUtils.isEmpty(mDeviceMac)) {
            if (!mDeviceMac.equals(bleDevice.mac, ignoreCase = true)) return
        }
        if (mDeviceNames != null && mDeviceNames!!.isNotEmpty()) {
            val equal = AtomicBoolean(false)
            for (name in mDeviceNames!!) {
                var remoteName = bleDevice.name
                if (remoteName == null) remoteName = ""
                if (if (mFuzzy) remoteName.contains(name) else remoteName == name) {
                    equal.set(true)
                }
            }
            if (!equal.get()) {
                return
            }
        }
        correctDeviceAndNextStep(bleDevice)
    }

    private fun correctDeviceAndNextStep(bleDevice: BleDevice) {
        if (mNeedConnect) {
            BleLog.i(
                "devices detected  ------"
                        + "  name:" + bleDevice.name
                        + "  mac:" + bleDevice.mac
                        + "  Rssi:" + bleDevice.rssi
                        + "  scanRecord:" + HexUtil.formatHexString(bleDevice.scanRecord)
            )
            mBleDeviceList.add(bleDevice)
            mMainHandler.post { BleScanner.instance.stopLeScan() }
        } else {
            val hasFound = AtomicBoolean(false)
            for (result in mBleDeviceList) {
                if (result.device == bleDevice.device) {
                    hasFound.set(true)
                }
            }
            if (!hasFound.get()) {
                BleLog.i(
                    "device detected  ------"
                            + "  name: " + bleDevice.name
                            + "  mac: " + bleDevice.mac
                            + "  Rssi: " + bleDevice.rssi
                            + "  scanRecord: " + HexUtil.formatHexString(bleDevice.scanRecord, true)
                )
                mBleDeviceList.add(bleDevice)
                mMainHandler.post { onScanning(bleDevice) }
            }
        }
    }

    fun notifyScanStarted(success: Boolean) {
        mBleDeviceList.clear()
        removeHandlerMsg()
        if (success && mScanTimeout > 0) {
            mMainHandler.postDelayed({ BleScanner.instance.stopLeScan() }, mScanTimeout)
        }
        mMainHandler.post { onScanStarted(success) }
    }

    fun notifyScanStopped() {
        mHandling = false
        mHandlerThread!!.quit()
        removeHandlerMsg()
        mMainHandler.post { onScanFinished(mBleDeviceList) }
    }

    fun removeHandlerMsg() {
        mMainHandler.removeCallbacksAndMessages(null)
        mHandler!!.removeCallbacksAndMessages(null)
    }

    abstract fun onScanStarted(success: Boolean)
    abstract fun onLeScan(bleDevice: BleDevice?)
    abstract fun onScanning(bleDevice: BleDevice?)
    abstract fun onScanFinished(bleDeviceList: List<BleDevice>?)
}