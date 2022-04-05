package com.app.okra.bluetooth.bluetooth

import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.app.okra.bluetooth.callback.*
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.data.BleMsg
import com.app.okra.bluetooth.data.BleWriteState
import com.app.okra.bluetooth.exception.GattException
import com.app.okra.bluetooth.exception.OtherException
import com.app.okra.bluetooth.exception.TimeoutException
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleConnector internal constructor(private val mBleBluetooth: BleBluetooth) {
    private val mBluetoothGatt: BluetoothGatt?
    private var mGattService: BluetoothGattService? = null
    private var mCharacteristic: BluetoothGattCharacteristic? = null
    private val mHandler: Handler
    private fun withUUID(serviceUUID: UUID?, characteristicUUID: UUID?): BleConnector {
        if (serviceUUID != null && mBluetoothGatt != null) {
            mGattService = mBluetoothGatt.getService(serviceUUID)
        }
        if (mGattService != null && characteristicUUID != null) {
            mCharacteristic = mGattService!!.getCharacteristic(characteristicUUID)
        }
        return this
    }

    fun withUUIDString(serviceUUID: String?, characteristicUUID: String?): BleConnector {
        return withUUID(formUUID(serviceUUID), formUUID(characteristicUUID))
    }
    /*------------------------------- main operation ----------------------------------- */
    /**
     * notify
     */
    fun enableCharacteristicNotify(
        bleNotifyCallback: BleNotifyCallback?, uuid_notify: String,
        userCharacteristicDescriptor: Boolean
    ) {
        if (mCharacteristic != null
            && mCharacteristic!!.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
        ) {
            handleCharacteristicNotifyCallback(bleNotifyCallback, uuid_notify)
            setCharacteristicNotification(
                mBluetoothGatt,
                mCharacteristic,
                userCharacteristicDescriptor,
                true,
                bleNotifyCallback
            )
        } else {
            bleNotifyCallback?.onNotifyFailure(OtherException("this characteristic not support notify!"))
        }
    }

    /**
     * stop notify
     */
    fun disableCharacteristicNotify(useCharacteristicDescriptor: Boolean): Boolean {
        return if (mCharacteristic != null
            && mCharacteristic!!.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
        ) {
            setCharacteristicNotification(
                mBluetoothGatt, mCharacteristic,
                useCharacteristicDescriptor, false, null
            )
        } else {
            false
        }
    }

    /**
     * notify setting
     */
    private fun setCharacteristicNotification(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        useCharacteristicDescriptor: Boolean,
        enable: Boolean,
        bleNotifyCallback: BleNotifyCallback?
    ): Boolean {
        if (gatt == null || characteristic == null) {
            notifyMsgInit()
            bleNotifyCallback?.onNotifyFailure(OtherException("gatt or characteristic equal null"))
            return false
        }
        val success1 = gatt.setCharacteristicNotification(characteristic, enable)
        if (!success1) {
            notifyMsgInit()
            bleNotifyCallback?.onNotifyFailure(OtherException("gatt setCharacteristicNotification fail"))
            return false
        }
        val descriptor: BluetoothGattDescriptor?
        descriptor = if (useCharacteristicDescriptor) {
            println(":::: characteristic.getDescriptor 1: " + characteristic.uuid)
            characteristic.getDescriptor(characteristic.uuid)
        } else {
            println(
                ":::: characteristic.getDescriptor 2: " + formUUID(
                    UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR
                )
            )
            characteristic.getDescriptor(formUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR))
        }
        return if (descriptor == null) {
            notifyMsgInit()
            bleNotifyCallback?.onNotifyFailure(OtherException("descriptor equals null"))
            false
        } else {
            println("::::: notification set:" + if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
            descriptor.value =
                if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            val success2 = gatt.writeDescriptor(descriptor)
            println("::::: writeDescriptor 1 :$success2")
            if (!success2) {
                notifyMsgInit()
                bleNotifyCallback?.onNotifyFailure(OtherException("gatt writeDescriptor fail"))
            }
            success2
        }
    }

    /*
     * indicate
     */
    fun enableCharacteristicIndicate(
        bleIndicateCallback: BleIndicateCallback?, uuid_indicate: String,
        useCharacteristicDescriptor: Boolean
    ) {
        if (mCharacteristic != null
            && mCharacteristic!!.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
        ) {
            handleCharacteristicIndicateCallback(bleIndicateCallback, uuid_indicate)
            setCharacteristicIndication(
                mBluetoothGatt, mCharacteristic,
                useCharacteristicDescriptor, true, bleIndicateCallback
            )
        } else {
            bleIndicateCallback?.onIndicateFailure(OtherException("this characteristic not support indicate!"))
        }
    }

    /**
     * stop indicate
     */
    fun disableCharacteristicIndicate(userCharacteristicDescriptor: Boolean): Boolean {
        return if (mCharacteristic != null
            && mCharacteristic!!.properties or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
        ) {
            setCharacteristicIndication(
                mBluetoothGatt, mCharacteristic,
                userCharacteristicDescriptor, false, null
            )
        } else {
            false
        }
    }

    /**
     * indicate setting
     */
    private fun setCharacteristicIndication(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        useCharacteristicDescriptor: Boolean,
        enable: Boolean,
        bleIndicateCallback: BleIndicateCallback?
    ): Boolean {
        if (gatt == null || characteristic == null) {
            indicateMsgInit()
            bleIndicateCallback?.onIndicateFailure(OtherException("gatt or characteristic equal null"))
            return false
        }
        val success1 = gatt.setCharacteristicNotification(characteristic, enable)
        if (!success1) {
            indicateMsgInit()
            bleIndicateCallback?.onIndicateFailure(OtherException("gatt setCharacteristicNotification fail"))
            return false
        }
        val descriptor: BluetoothGattDescriptor?
        descriptor = if (useCharacteristicDescriptor) {
            characteristic.getDescriptor(characteristic.uuid)
        } else {
            characteristic.getDescriptor(formUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR))
        }
        return if (descriptor == null) {
            indicateMsgInit()
            bleIndicateCallback?.onIndicateFailure(OtherException("descriptor equals null"))
            false
        } else {
            descriptor.value =
                if (enable) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            val success2 = gatt.writeDescriptor(descriptor)
            println("::::: writeDescriptor 2:$success2")
            if (!success2) {
                indicateMsgInit()
                bleIndicateCallback?.onIndicateFailure(OtherException("gatt writeDescriptor fail"))
            }
            success2
        }
    }

    /**
     * write
     */
    fun writeCharacteristic(
        data: ByteArray?,
        bleWriteCallback: BleWriteCallback?,
        uuid_write: String
    ) {
        if (data == null || data.size <= 0) {
            bleWriteCallback?.onWriteFailure(OtherException("the data to be written is empty"))
            return
        }
        if (mCharacteristic == null
            || mCharacteristic!!.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0
        ) {
            bleWriteCallback?.onWriteFailure(OtherException("this characteristic not support write!"))
            return
        }
        if (mCharacteristic!!.setValue(data)) {
            handleCharacteristicWriteCallback(bleWriteCallback, uuid_write)
            if (!mBluetoothGatt!!.writeCharacteristic(mCharacteristic)) {
                writeMsgInit()
                bleWriteCallback?.onWriteFailure(OtherException("gatt writeCharacteristic fail"))
            }
        } else {
            bleWriteCallback?.onWriteFailure(OtherException("Updates the locally stored value of this characteristic fail"))
        }
    }

    /**
     * read
     */
    fun readCharacteristic(bleReadCallback: BleReadCallback?, uuid_read: String) {
        if (mCharacteristic != null
            && mCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_READ > 0
        ) {
            handleCharacteristicReadCallback(bleReadCallback, uuid_read)
            if (!mBluetoothGatt!!.readCharacteristic(mCharacteristic)) {
                readMsgInit()
                bleReadCallback?.onReadFailure(OtherException("gatt readCharacteristic fail"))
            }
        } else {
            bleReadCallback?.onReadFailure(OtherException("this characteristic not support read!"))
        }
    }

    /**
     * rssi
     */
    fun readRemoteRssi(bleRssiCallback: BleRssiCallback?) {
        handleRSSIReadCallback(bleRssiCallback)
        if (!mBluetoothGatt!!.readRemoteRssi()) {
            rssiMsgInit()
            bleRssiCallback?.onRssiFailure(OtherException("gatt readRemoteRssi fail"))
        }
    }

    /**
     * set mtu
     */
    fun setMtu(requiredMtu: Int, bleMtuChangedCallback: BleMtuChangedCallback?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            handleSetMtuCallback(bleMtuChangedCallback)
            if (!mBluetoothGatt!!.requestMtu(requiredMtu)) {
                mtuChangedMsgInit()
                bleMtuChangedCallback?.onSetMTUFailure(OtherException("gatt requestMtu fail"))
            }
        } else {
            bleMtuChangedCallback?.onSetMTUFailure(OtherException("API level lower than 21"))
        }
    }

    /**
     * requestConnectionPriority
     *
     * @param connectionPriority Request a specific connection priority. Must be one of
     * [BluetoothGatt.CONNECTION_PRIORITY_BALANCED],
     * [BluetoothGatt.CONNECTION_PRIORITY_HIGH]
     * or [BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER].
     * @throws IllegalArgumentException If the parameters are outside of their
     * specified range.
     */
    fun requestConnectionPriority(connectionPriority: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt!!.requestConnectionPriority(connectionPriority)
        } else false
    }
    /**************************************** Handle call back  */
    /**
     * notify
     */
    private fun handleCharacteristicNotifyCallback(
        bleNotifyCallback: BleNotifyCallback?,
        uuid_notify: String
    ) {
        if (bleNotifyCallback != null) {
            notifyMsgInit()
            bleNotifyCallback.key = uuid_notify
            bleNotifyCallback.handler = mHandler
            mBleBluetooth.addNotifyCallback(uuid_notify, bleNotifyCallback)
            mHandler.sendMessageDelayed(
                mHandler.obtainMessage(BleMsg.MSG_CHA_NOTIFY_START, bleNotifyCallback),
                BleManager.instance.operateTimeout.toLong()
            )
        }
    }

    /**
     * indicate
     */
    private fun handleCharacteristicIndicateCallback(
        bleIndicateCallback: BleIndicateCallback?,
        uuid_indicate: String
    ) {
        if (bleIndicateCallback != null) {
            indicateMsgInit()
            bleIndicateCallback.key = uuid_indicate
            bleIndicateCallback.handler = mHandler
            mBleBluetooth.addIndicateCallback(uuid_indicate, bleIndicateCallback)
            mHandler.sendMessageDelayed(
                mHandler.obtainMessage(BleMsg.MSG_CHA_INDICATE_START, bleIndicateCallback),
                BleManager.instance.operateTimeout.toLong()
            )
        }
    }

    /**
     * write
     */
    private fun handleCharacteristicWriteCallback(
        bleWriteCallback: BleWriteCallback?,
        uuid_write: String
    ) {
        if (bleWriteCallback != null) {
            writeMsgInit()
            bleWriteCallback.key = uuid_write
            bleWriteCallback.handler = mHandler
            mBleBluetooth.addWriteCallback(uuid_write, bleWriteCallback)
            mHandler.sendMessageDelayed(
                mHandler.obtainMessage(BleMsg.MSG_CHA_WRITE_START, bleWriteCallback),
                BleManager.instance.operateTimeout.toLong()
            )
        }
    }

    /**
     * read
     */
    private fun handleCharacteristicReadCallback(
        bleReadCallback: BleReadCallback?,
        uuid_read: String
    ) {
        if (bleReadCallback != null) {
            readMsgInit()
            bleReadCallback.key = uuid_read
            bleReadCallback.handler = mHandler
            mBleBluetooth.addReadCallback(uuid_read, bleReadCallback)
            mHandler.sendMessageDelayed(
                mHandler.obtainMessage(BleMsg.MSG_CHA_READ_START, bleReadCallback),
                BleManager.instance.operateTimeout.toLong()
            )
        }
    }

    /**
     * rssi
     */
    private fun handleRSSIReadCallback(bleRssiCallback: BleRssiCallback?) {
        if (bleRssiCallback != null) {
            rssiMsgInit()
            bleRssiCallback.handler = mHandler
            mBleBluetooth.addRssiCallback(bleRssiCallback)
            mHandler.sendMessageDelayed(
                mHandler.obtainMessage(BleMsg.MSG_READ_RSSI_START, bleRssiCallback),
                BleManager.instance.operateTimeout.toLong()
            )
        }
    }

    /**
     * set mtu
     */
    private fun handleSetMtuCallback(bleMtuChangedCallback: BleMtuChangedCallback?) {
        if (bleMtuChangedCallback != null) {
            mtuChangedMsgInit()
            bleMtuChangedCallback.handler = mHandler
            mBleBluetooth.addMtuChangedCallback(bleMtuChangedCallback)
            mHandler.sendMessageDelayed(
                mHandler.obtainMessage(BleMsg.MSG_SET_MTU_START, bleMtuChangedCallback),
                BleManager.instance.operateTimeout.toLong()
            )
        }
    }

    fun notifyMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_NOTIFY_START)
    }

    fun indicateMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_INDICATE_START)
    }

    fun writeMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_WRITE_START)
    }

    fun readMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_READ_START)
    }

    fun rssiMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_READ_RSSI_START)
    }

    fun mtuChangedMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_SET_MTU_START)
    }

    companion object {
        private const val UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR =
            "00002902-0000-1000-8000-00805f9b34fb"
        const val UUID_CLIENT_SERVICE_DESCRIPTOR = "0000ffe0-0000-1000-8000-00805f9b34fb"
        const val UUID_CLIENT_CHARACTERISTIC_DESCRIPTOR = "0000ffe4-0000-1000-8000-00805f9b34fb"
        const val UUID_CLIENT_WRITE_SERVICE_DESCRIPTOR = "0000ffe5-0000-1000-8000-00805f9b34fb"
        const val UUID_CLIENT_WRITE_CHARACTERISTIC_DESCRIPTOR =
            "0000ffe9-0000-1000-8000-00805f9b34fb"

        val mByte_ForCount = byteArrayOf(
            0x80.toByte(), 0x0F.toByte(), 0xF0.toByte(), 0x01.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        )

        val mByte_ForData = byteArrayOf(
            0x80.toByte(), 0x0F.toByte(), 0xF0.toByte(), 0x02.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        )


        @JvmStatic
        fun formUUID(uuid: String?): UUID? {
            return if (uuid == null) null else UUID.fromString(uuid)
        }
    }

    init {
        mBluetoothGatt = mBleBluetooth.bluetoothGatt
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    BleMsg.MSG_CHA_NOTIFY_START -> {
                        val notifyCallback = msg.obj as BleNotifyCallback
                        notifyCallback?.onNotifyFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_NOTIFY_RESULT -> {
                        notifyMsgInit()
                        val notifyCallback = msg.obj as BleNotifyCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_NOTIFY_BUNDLE_STATUS)
                        if (notifyCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                notifyCallback.onNotifySuccess()
                            } else {
                                notifyCallback.onNotifyFailure(GattException(status))
                            }
                        }
                    }
                    BleMsg.MSG_CHA_NOTIFY_DATA_CHANGE -> {
                        val notifyCallback = msg.obj as BleNotifyCallback
                        val bundle = msg.data
                        val value = bundle.getByteArray(BleMsg.KEY_NOTIFY_BUNDLE_VALUE)
                        notifyCallback?.onCharacteristicChanged(value)
                    }
                    BleMsg.MSG_CHA_INDICATE_START -> {
                        val indicateCallback = msg.obj as BleIndicateCallback
                        indicateCallback?.onIndicateFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_INDICATE_RESULT -> {
                        indicateMsgInit()
                        val indicateCallback = msg.obj as BleIndicateCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_INDICATE_BUNDLE_STATUS)
                        if (indicateCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                indicateCallback.onIndicateSuccess()
                            } else {
                                indicateCallback.onIndicateFailure(GattException(status))
                            }
                        }
                    }
                    BleMsg.MSG_CHA_INDICATE_DATA_CHANGE -> {
                        val indicateCallback = msg.obj as BleIndicateCallback
                        val bundle = msg.data
                        val value = bundle.getByteArray(BleMsg.KEY_INDICATE_BUNDLE_VALUE)
                        indicateCallback?.onCharacteristicChanged(value)
                    }
                    BleMsg.MSG_CHA_WRITE_START -> {
                        val writeCallback = msg.obj as BleWriteCallback
                        writeCallback?.onWriteFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_WRITE_RESULT -> {
                        writeMsgInit()
                        val writeCallback = msg.obj as BleWriteCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_WRITE_BUNDLE_STATUS)
                        val value = bundle.getByteArray(BleMsg.KEY_WRITE_BUNDLE_VALUE)
                        if (writeCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                writeCallback.onWriteSuccess(
                                    BleWriteState.DATA_WRITE_SINGLE,
                                    BleWriteState.DATA_WRITE_SINGLE,
                                    value
                                )
                            } else {
                                writeCallback.onWriteFailure(GattException(status))
                            }
                        }
                    }
                    BleMsg.MSG_CHA_READ_START -> {
                        val readCallback = msg.obj as BleReadCallback
                        readCallback?.onReadFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_READ_RESULT -> {
                        readMsgInit()
                        val readCallback = msg.obj as BleReadCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_READ_BUNDLE_STATUS)
                        val value = bundle.getByteArray(BleMsg.KEY_READ_BUNDLE_VALUE)
                        if (readCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                readCallback.onReadSuccess(value)
                            } else {
                                readCallback.onReadFailure(GattException(status))
                            }
                        }
                    }
                    BleMsg.MSG_READ_RSSI_START -> {
                        val rssiCallback = msg.obj as BleRssiCallback
                        rssiCallback?.onRssiFailure(TimeoutException())
                    }
                    BleMsg.MSG_READ_RSSI_RESULT -> {
                        rssiMsgInit()
                        val rssiCallback = msg.obj as BleRssiCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_STATUS)
                        val value = bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_VALUE)
                        if (rssiCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                rssiCallback.onRssiSuccess(value)
                            } else {
                                rssiCallback.onRssiFailure(GattException(status))
                            }
                        }
                    }
                    BleMsg.MSG_SET_MTU_START -> {
                        val mtuChangedCallback = msg.obj as BleMtuChangedCallback
                        mtuChangedCallback?.onSetMTUFailure(TimeoutException())
                    }
                    BleMsg.MSG_SET_MTU_RESULT -> {
                        mtuChangedMsgInit()
                        val mtuChangedCallback = msg.obj as BleMtuChangedCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_STATUS)
                        val value = bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_VALUE)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            mtuChangedCallback.onMtuChanged(value)
                        } else {
                            mtuChangedCallback.onSetMTUFailure(GattException(status))
                        }
                    }
                }
            }
        }
    }
}