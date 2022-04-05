package com.app.okra.bluetooth.bluetooth

import android.bluetooth.BluetoothDevice
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.bluetooth.utils.BleLruHashMap
import java.util.*

class MultipleBluetoothController {
    private val bleLruHashMap: BleLruHashMap<String, BleBluetooth> = BleLruHashMap(BleManager.instance.maxConnectCount)
    private val bleTempHashMap: HashMap<String, BleBluetooth>
    @Synchronized
    fun buildConnectingBle(bleDevice: BleDevice?): BleBluetooth? {
        bleDevice?.let {
            val bleBluetooth = BleBluetooth(bleDevice)
            if (!bleTempHashMap.containsKey(bleBluetooth.deviceKey)) {
                bleTempHashMap[bleBluetooth.deviceKey] = bleBluetooth
            }
            return bleBluetooth
        }
        return null
    }

    @Synchronized
    fun removeConnectingBle(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (bleTempHashMap.containsKey(bleBluetooth.deviceKey)) {
            bleTempHashMap.remove(bleBluetooth.deviceKey)
        }
    }

    @Synchronized
    fun addBleBluetooth(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (!bleLruHashMap.containsKey(bleBluetooth.deviceKey)) {
            bleLruHashMap[bleBluetooth.deviceKey] = bleBluetooth
        }
    }

    @Synchronized
    fun removeBleBluetooth(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (bleLruHashMap.containsKey(bleBluetooth.deviceKey)) {
            bleLruHashMap.remove(bleBluetooth.deviceKey)
        }
    }

    @Synchronized
    fun isContainDevice(bleDevice: BleDevice?): Boolean {
        return bleDevice != null && bleLruHashMap.containsKey(bleDevice.deviceKey)
    }

    @Synchronized
    fun isContainDevice(bluetoothDevice: BluetoothDevice?): Boolean {
        return bluetoothDevice != null && bleLruHashMap.containsKey(bluetoothDevice.name + bluetoothDevice.address)
    }

    @Synchronized
    fun getBleBluetooth(bleDevice: BleDevice?): BleBluetooth? {
        if (bleDevice != null) {
            if (bleLruHashMap.containsKey(bleDevice.deviceKey)) {
                return bleLruHashMap[bleDevice.deviceKey]
            }
        }
        return null
    }

    @Synchronized
    fun disconnect(bleDevice: BleDevice?) {
        if (isContainDevice(bleDevice)) {
            getBleBluetooth(bleDevice)!!.disconnect()
        }
    }

    @Synchronized
    fun disconnectAllDevice() {
        for ((_, value) in bleLruHashMap) {
            value.disconnect()
        }
        bleLruHashMap.clear()
    }

    @Synchronized
    fun destroy() {
        for ((_, value) in bleLruHashMap) {
            value.destroy()
        }
        bleLruHashMap.clear()
        for ((_, value) in bleTempHashMap) {
            value.destroy()
        }
        bleTempHashMap.clear()
    }

    @get:Synchronized
    val bleBluetoothList: List<BleBluetooth>
        get() {
            val bleBluetoothList: List<BleBluetooth> = ArrayList(bleLruHashMap.values)
            Collections.sort(bleBluetoothList) { lhs, rhs ->
                lhs.deviceKey.compareTo(
                    rhs.deviceKey,
                    ignoreCase = true
                )
            }
            return bleBluetoothList
        }

    @get:Synchronized
    val deviceList: List<BleDevice>
        get() {
            refreshConnectedDevice()
            val deviceList: MutableList<BleDevice> = ArrayList()
            for (BleBluetooth in bleBluetoothList) {
                deviceList.add(BleBluetooth.device)
            }
            return deviceList
        }

    fun refreshConnectedDevice() {
        val bluetoothList = bleBluetoothList
        var i = 0
        while (i < bluetoothList.size) {
            val bleBluetooth = bluetoothList[i]
            if (!BleManager.instance.isConnected(bleBluetooth.device)) {
                removeBleBluetooth(bleBluetooth)
            }
            i++
        }
    }

    init {
        bleTempHashMap = HashMap()
    }
}