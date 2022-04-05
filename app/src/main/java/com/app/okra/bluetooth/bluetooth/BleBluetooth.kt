package com.app.okra.bluetooth.bluetooth

import android.annotation.TargetApi
import android.bluetooth.*
import android.os.*
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.bluetooth.BleConnector.Companion.formUUID
import com.app.okra.bluetooth.callback.*
import com.app.okra.bluetooth.data.BleConnectStateParameter
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.bluetooth.data.BleMsg
import com.app.okra.bluetooth.exception.ConnectException
import com.app.okra.bluetooth.exception.OtherException
import com.app.okra.bluetooth.exception.TimeoutException
import com.app.okra.bluetooth.utils.BleLog
import com.app.okra.utils.Listeners
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleBluetooth(val device: BleDevice) {
    private var bleGattCallback: BleGattCallback? = null
    private var bleRssiCallback: BleRssiCallback? = null
    private var bleMtuChangedCallback: BleMtuChangedCallback? = null
    private val bleNotifyCallbackHashMap: HashMap<String, BleNotifyCallback>? = HashMap()
    private val bleIndicateCallbackHashMap: HashMap<String, BleIndicateCallback>? = HashMap()
    private val bleWriteCallbackHashMap: HashMap<String, BleWriteCallback>? = HashMap()
    private val bleReadCallbackHashMap: HashMap<String, BleReadCallback>? = HashMap()
    private var lastState: LastState? = null
    private var isActiveDisconnect = false
    var bluetoothGatt: BluetoothGatt? = null
        private set
    private val mainHandler = MainHandler(Looper.getMainLooper())
    val deviceKey = device.deviceKey
    private var connectRetryCount = 0
    private var bleDataCallback: Listeners.BLEDataListener?=null
    fun newBleConnector(): BleConnector {
        return BleConnector(this)
    }

    @Synchronized
    fun addConnectGattCallback(callback: BleGattCallback?) {
        bleGattCallback = callback
    }

    @Synchronized
    fun removeConnectGattCallback() {
        bleGattCallback = null
    }

    @Synchronized
    fun addNotifyCallback(uuid: String, bleNotifyCallback: BleNotifyCallback) {
        bleNotifyCallbackHashMap!![uuid] = bleNotifyCallback
    }

    @Synchronized
    fun addOwnCallback(bleDataCallback: Listeners.BLEDataListener) {
        this.bleDataCallback = bleDataCallback
    }

    @Synchronized
    fun addIndicateCallback(uuid: String, bleIndicateCallback: BleIndicateCallback) {
        bleIndicateCallbackHashMap!![uuid] = bleIndicateCallback
    }

    @Synchronized
    fun addWriteCallback(uuid: String, bleWriteCallback: BleWriteCallback) {
        bleWriteCallbackHashMap!![uuid] = bleWriteCallback
    }

    @Synchronized
    fun addReadCallback(uuid: String, bleReadCallback: BleReadCallback) {
        bleReadCallbackHashMap!![uuid] = bleReadCallback
    }

    @Synchronized
    fun removeNotifyCallback(uuid: String) {
        if (bleNotifyCallbackHashMap!!.containsKey(uuid)) bleNotifyCallbackHashMap.remove(uuid)
    }

    @Synchronized
    fun removeIndicateCallback(uuid: String) {
        if (bleIndicateCallbackHashMap!!.containsKey(uuid)) bleIndicateCallbackHashMap.remove(uuid)
    }

    @Synchronized
    fun removeWriteCallback(uuid: String) {
        if (bleWriteCallbackHashMap!!.containsKey(uuid)) bleWriteCallbackHashMap.remove(uuid)
    }

    @Synchronized
    fun removeReadCallback(uuid: String) {
        if (bleReadCallbackHashMap!!.containsKey(uuid)) bleReadCallbackHashMap.remove(uuid)
    }

    @Synchronized
    fun clearCharacterCallback() {
        bleNotifyCallbackHashMap?.clear()
        bleIndicateCallbackHashMap?.clear()
        bleWriteCallbackHashMap?.clear()
        bleReadCallbackHashMap?.clear()
    }

    @Synchronized
    fun addRssiCallback(callback: BleRssiCallback?) {
        bleRssiCallback = callback
    }

    @Synchronized
    fun removeRssiCallback() {
        bleRssiCallback = null
    }

    @Synchronized
    fun addMtuChangedCallback(callback: BleMtuChangedCallback?) {
        bleMtuChangedCallback = callback
    }

    @Synchronized
    fun removeMtuChangedCallback() {
        bleMtuChangedCallback = null
    }

    @Synchronized
    fun connect(
        bleDevice: BleDevice,
        autoConnect: Boolean,
        callback: BleGattCallback?
    ): BluetoothGatt? {
        return connect(bleDevice, autoConnect, callback, 0)
    }

    @Synchronized
    fun connect(
        bleDevice: BleDevice,
        autoConnect: Boolean,
        callback: BleGattCallback?,
        connectRetryCount: Int
    ): BluetoothGatt? {
        BleLog.i(
            """
    connect device: ${bleDevice.name}
    mac: ${bleDevice.mac}
    autoConnect: $autoConnect
    currentThread: ${Thread.currentThread().id}
    connectCount:${connectRetryCount + 1}
    """.trimIndent()
        )
        if (connectRetryCount == 0) {
            this.connectRetryCount = 0
        }
        addConnectGattCallback(callback)
        lastState = LastState.CONNECT_CONNECTING
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bleDevice.device!!.connectGatt(
                BleManager.instance.getContext(),
                autoConnect,
                coreGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } else {
            bleDevice.device!!.connectGatt(
                BleManager.instance.getContext(),
                autoConnect,
                coreGattCallback
            )
        }
        if (bluetoothGatt != null) {
            if (bleGattCallback != null) {
                bleGattCallback!!.onStartConnect()
            }
            val message = mainHandler.obtainMessage()
            message.what = BleMsg.MSG_CONNECT_OVER_TIME
            mainHandler.sendMessageDelayed(message, BleManager.instance.connectOverTime)
        } else {
            disconnectGatt()
            refreshDeviceCache()
            closeBluetoothGatt()
            lastState = LastState.CONNECT_FAILURE
            BleManager.instance.multipleBluetoothController
                ?.removeConnectingBle(this@BleBluetooth)
            if (bleGattCallback != null) bleGattCallback!!.onConnectFail(
                bleDevice,
                OtherException("GATT connect exception occurred!")
            )
        }
        return bluetoothGatt
    }

    @Synchronized
    fun disconnect() {
        isActiveDisconnect = true
        disconnectGatt()
    }

    @Synchronized
    fun destroy() {
        lastState = LastState.CONNECT_IDLE
        disconnectGatt()
        refreshDeviceCache()
        closeBluetoothGatt()
        removeConnectGattCallback()
        removeRssiCallback()
        removeMtuChangedCallback()
        clearCharacterCallback()
        mainHandler.removeCallbacksAndMessages(null)
    }

    @Synchronized
    private fun disconnectGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt!!.disconnect()
        }
    }

    @Synchronized
    private fun refreshDeviceCache() {
        try {
            val refresh = BluetoothGatt::class.java.getMethod("refresh")
            if (refresh != null && bluetoothGatt != null) {
                val success = refresh.invoke(bluetoothGatt) as Boolean
                BleLog.i("refreshDeviceCache, is success:  $success")
            }
        } catch (e: Exception) {
            BleLog.i("exception occur while refreshing device: " + e.message)
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun closeBluetoothGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt!!.close()
        }
    }

    private inner class MainHandler internal constructor(looper: Looper?) : Handler(
        looper!!
    ) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BleMsg.MSG_CONNECT_FAIL -> {
                    disconnectGatt()
                    refreshDeviceCache()
                    closeBluetoothGatt()
                    if (connectRetryCount < BleManager.instance.reConnectCount) {
                        BleLog.e(
                            "Connect fail, try reconnect " + BleManager.instance
                                .reConnectInterval.toString() + " millisecond later"
                        )
                        ++connectRetryCount
                        val message = mainHandler.obtainMessage()
                        message.what = BleMsg.MSG_RECONNECT
                        mainHandler.sendMessageDelayed(
                            message,
                            BleManager.instance.reConnectInterval
                        )
                    } else {
                        lastState = LastState.CONNECT_FAILURE
                        BleManager.instance.multipleBluetoothController
                            ?.removeConnectingBle(this@BleBluetooth)
                        val para = msg.obj as BleConnectStateParameter
                        val status = para.status
                        if (bleGattCallback != null) bleGattCallback!!.onConnectFail(
                            device,
                            ConnectException(bluetoothGatt!!, status)
                        )
                    }
                }
                BleMsg.MSG_DISCONNECTED -> {
                    lastState = LastState.CONNECT_DISCONNECT
                    BleManager.instance.multipleBluetoothController
                        ?.removeBleBluetooth(this@BleBluetooth)
                    disconnect()
                    refreshDeviceCache()
                    closeBluetoothGatt()
                    removeRssiCallback()
                    removeMtuChangedCallback()
                    clearCharacterCallback()
                    mainHandler.removeCallbacksAndMessages(null)
                    val para = msg.obj as BleConnectStateParameter
                    val isActive = para.isActive
                    val status = para.status
                    if (bleGattCallback != null) bleGattCallback!!.onDisConnected(
                        isActive,
                        device,
                        bluetoothGatt,
                        status
                    )
                }
                BleMsg.MSG_RECONNECT -> {
                    connect(device, false, bleGattCallback, connectRetryCount)
                }
                BleMsg.MSG_CONNECT_OVER_TIME -> {
                    disconnectGatt()
                    refreshDeviceCache()
                    closeBluetoothGatt()
                    lastState = LastState.CONNECT_FAILURE
                    BleManager.instance.multipleBluetoothController
                        ?.removeConnectingBle(this@BleBluetooth)
                    if (bleGattCallback != null) bleGattCallback!!.onConnectFail(
                        device,
                        TimeoutException()
                    )
                }
                BleMsg.MSG_DISCOVER_SERVICES -> {
                    if (bluetoothGatt != null) {
                        val discoverServiceResult = bluetoothGatt!!.discoverServices()
                        if (!discoverServiceResult) {
                            val message = mainHandler.obtainMessage()
                            message.what = BleMsg.MSG_DISCOVER_FAIL
                            mainHandler.sendMessage(message)
                        }
                    } else {
                        val message = mainHandler.obtainMessage()
                        message.what = BleMsg.MSG_DISCOVER_FAIL
                        mainHandler.sendMessage(message)
                    }
                }
                BleMsg.MSG_DISCOVER_FAIL -> {
                    disconnectGatt()
                    refreshDeviceCache()
                    closeBluetoothGatt()
                    lastState = LastState.CONNECT_FAILURE
                    BleManager.instance.multipleBluetoothController?.removeConnectingBle(this@BleBluetooth)
                    if (bleGattCallback != null) bleGattCallback!!.onConnectFail(
                        device,
                        OtherException("GATT discover services exception occurred!")
                    )
                }
                BleMsg.MSG_DISCOVER_SUCCESS -> {
                    lastState = LastState.CONNECT_CONNECTED
                    isActiveDisconnect = false
                    BleManager.instance.multipleBluetoothController
                        ?.removeConnectingBle(this@BleBluetooth)
                    BleManager.instance.multipleBluetoothController
                        ?.addBleBluetooth(this@BleBluetooth)
                    val para = msg.obj as BleConnectStateParameter
                    val status = para.status
                    if (bleGattCallback != null) bleGattCallback!!.onConnectSuccess(
                        device,
                        bluetoothGatt,
                        status
                    )
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val coreGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            BleLog.i(
                """
    BluetoothGattCallback：onConnectionStateChange 
    status: $status
    newState: $newState
    currentThread: ${Thread.currentThread().id}
    """.trimIndent()
            )
            bluetoothGatt = gatt
            mainHandler.removeMessages(BleMsg.MSG_CONNECT_OVER_TIME)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                val message = mainHandler.obtainMessage()
                message.what = BleMsg.MSG_DISCOVER_SERVICES
                mainHandler.sendMessageDelayed(message, 500)
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (lastState == LastState.CONNECT_CONNECTING) {
                    val message = mainHandler.obtainMessage()
                    message.what = BleMsg.MSG_CONNECT_FAIL
                    message.obj = BleConnectStateParameter(status)
                    mainHandler.sendMessage(message)
                } else if (lastState == LastState.CONNECT_CONNECTED) {
                    val message = mainHandler.obtainMessage()
                    message.what = BleMsg.MSG_DISCONNECTED
                    val para = BleConnectStateParameter(status)
                    para.isActive = isActiveDisconnect
                    message.obj = para
                    mainHandler.sendMessage(message)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            BleLog.i(
                """
    BluetoothGattCallback：onServicesDiscovered 
    status: $status
    currentThread: ${Thread.currentThread().id}
    """.trimIndent()
            )
            bluetoothGatt = gatt
            val bleChar =
                bluetoothGatt!!.getService(formUUID(BleConnector.UUID_CLIENT_SERVICE_DESCRIPTOR))
                    .getCharacteristic(formUUID(BleConnector.UUID_CLIENT_CHARACTERISTIC_DESCRIPTOR))
            bluetoothGatt!!.readCharacteristic(bleChar)
            bluetoothGatt!!.setCharacteristicNotification(bleChar, true)
            bleChar.value = byteArrayOf(1)
            bluetoothGatt!!.writeCharacteristic(bleChar)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val message = mainHandler.obtainMessage()
                message.what = BleMsg.MSG_DISCOVER_SUCCESS
                message.obj = BleConnectStateParameter(status)
                mainHandler.sendMessage(message)
            } else {
                val message = mainHandler.obtainMessage()
                message.what = BleMsg.MSG_DISCOVER_FAIL
                mainHandler.sendMessage(message)
            }
            }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            BleLog.i(
                """
    BluetoothGattCallback：onCharacteristicChanged 
    status: $characteristic.value
    currentThread: ${Thread.currentThread().id}
    """.trimIndent()
            )
            println("::::: onCharacteristicChanged: " + characteristic.value)

            println("::::: Data: ")

            for (i in characteristic.value.indices) {
                print(" " + characteristic.value[i])
            }
            println()

            bleDataCallback?.onDataReceived(characteristic.value)

            var iterator: Iterator<*> = bleNotifyCallbackHashMap!!.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next() as Map.Entry<*, *>
                val callback = entry.value!!
                if (callback is BleNotifyCallback) {
                    if (characteristic.uuid.toString().equals(callback.key, ignoreCase = true)) {
                        val handler = callback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_NOTIFY_DATA_CHANGE
                            message.obj = callback
                            val bundle = Bundle()
                            bundle.putByteArray(
                                BleMsg.KEY_NOTIFY_BUNDLE_VALUE,
                                characteristic.value
                            )
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
            iterator = bleIndicateCallbackHashMap!!.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next() as Map.Entry<*, *>
                val callback = entry.value!!
                if (callback is BleIndicateCallback) {
                    val bleIndicateCallback = callback
                    if (characteristic.uuid.toString()
                            .equals(bleIndicateCallback.key, ignoreCase = true)
                    ) {
                        val handler = bleIndicateCallback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_INDICATE_DATA_CHANGE
                            message.obj = bleIndicateCallback
                            val bundle = Bundle()
                            bundle.putByteArray(
                                BleMsg.KEY_INDICATE_BUNDLE_VALUE,
                                characteristic.value
                            )
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            BleLog.i(
                """
    BluetoothGattCallback：onDescriptorWrite 
    status: $status
    currentThread: ${Thread.currentThread().id}
    """.trimIndent()
            )
            println("::::: Descriptor Write: ")
            var iterator: Iterator<*> = bleNotifyCallbackHashMap!!.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next() as Map.Entry<*, *>
                val callback = entry.value!!
                if (callback is BleNotifyCallback) {
                    val bleNotifyCallback = callback
                    if (descriptor.characteristic.uuid.toString()
                            .equals(bleNotifyCallback.key, ignoreCase = true)
                    ) {

                        /* BluetoothGattCharacteristic characteristic =
                                 gatt.getService(BleConnector.formUUID(UUID_CLIENT_WRITE_SERVICE_DESCRIPTOR))
                                .getCharacteristic(BleConnector.formUUID(UUID_CLIENT_WRITE_CHARACTERISTIC_DESCRIPTOR));

                        characteristic.setValue(new byte[]{1, 1});

                        gatt.writeCharacteristic(characteristic);*/
                        val handler = bleNotifyCallback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_NOTIFY_RESULT
                            message.obj = bleNotifyCallback
                            val bundle = Bundle()
                            bundle.putInt(BleMsg.KEY_NOTIFY_BUNDLE_STATUS, status)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
            iterator = bleIndicateCallbackHashMap!!.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next() as Map.Entry<*, *>
                val callback = entry.value!!
                if (callback is BleIndicateCallback) {
                    val bleIndicateCallback = callback
                    if (descriptor.characteristic.uuid.toString()
                            .equals(bleIndicateCallback.key, ignoreCase = true)
                    ) {
                        val handler = bleIndicateCallback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_INDICATE_RESULT
                            message.obj = bleIndicateCallback
                            val bundle = Bundle()
                            bundle.putInt(BleMsg.KEY_INDICATE_BUNDLE_STATUS, status)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            BleLog.i(
                """
    BluetoothGattCallback：onCharacteristicWrite 
    status: $status
    currentThread: ${Thread.currentThread().id}
    """.trimIndent()
            )
            println("::::: onCharacteristicWrite: ")
            val iterator: Iterator<*> = bleWriteCallbackHashMap!!.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next() as Map.Entry<*, *>
                val callback = entry.value!!
                if (callback is BleWriteCallback) {
                    val bleWriteCallback = callback
                    if (characteristic.uuid.toString()
                            .equals(bleWriteCallback.key, ignoreCase = true)
                    ) {
                        val handler = bleWriteCallback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_WRITE_RESULT
                            message.obj = bleWriteCallback
                            val bundle = Bundle()
                            bundle.putInt(BleMsg.KEY_WRITE_BUNDLE_STATUS, status)
                            bundle.putByteArray(BleMsg.KEY_WRITE_BUNDLE_VALUE, characteristic.value)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            println("::::: onCharacteristicRead: ")
            BleLog.i(
                """
    BluetoothGattCallback：onCharacteristicRead 
    status: $status
    currentThread: ${Thread.currentThread().id}
    """.trimIndent()
            )
            val iterator: Iterator<*> = bleReadCallbackHashMap!!.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next() as Map.Entry<*, *>
                val callback = entry.value!!
                if (callback is BleReadCallback) {
                    val bleReadCallback = callback
                    if (characteristic.uuid.toString()
                            .equals(bleReadCallback.key, ignoreCase = true)
                    ) {
                        val handler = bleReadCallback.handler
                        if (handler != null) {
                            val message = handler.obtainMessage()
                            message.what = BleMsg.MSG_CHA_READ_RESULT
                            message.obj = bleReadCallback
                            val bundle = Bundle()
                            bundle.putInt(BleMsg.KEY_READ_BUNDLE_STATUS, status)
                            bundle.putByteArray(BleMsg.KEY_READ_BUNDLE_VALUE, characteristic.value)
                            message.data = bundle
                            handler.sendMessage(message)
                        }
                    }
                }
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            println("::::: onReadRemoteRssi: ")
            if (bleRssiCallback != null) {
                val handler = bleRssiCallback!!.handler
                if (handler != null) {
                    val message = handler.obtainMessage()
                    message.what = BleMsg.MSG_READ_RSSI_RESULT
                    message.obj = bleRssiCallback
                    val bundle = Bundle()
                    bundle.putInt(BleMsg.KEY_READ_RSSI_BUNDLE_STATUS, status)
                    bundle.putInt(BleMsg.KEY_READ_RSSI_BUNDLE_VALUE, rssi)
                    message.data = bundle
                    handler.sendMessage(message)
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            println("::::: onMtuChanged: ")
            if (bleMtuChangedCallback != null) {
                val handler = bleMtuChangedCallback!!.handler
                if (handler != null) {
                    val message = handler.obtainMessage()
                    message.what = BleMsg.MSG_SET_MTU_RESULT
                    message.obj = bleMtuChangedCallback
                    val bundle = Bundle()
                    bundle.putInt(BleMsg.KEY_SET_MTU_BUNDLE_STATUS, status)
                    bundle.putInt(BleMsg.KEY_SET_MTU_BUNDLE_VALUE, mtu)
                    message.data = bundle
                    handler.sendMessage(message)
                }
            }
        }
    }

    internal enum class LastState {
        CONNECT_IDLE, CONNECT_CONNECTING, CONNECT_CONNECTED, CONNECT_FAILURE, CONNECT_DISCONNECT
    }
}