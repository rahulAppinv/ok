package com.app.okra.bluetooth

import android.annotation.TargetApi
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import com.app.okra.bluetooth.bluetooth.BleBluetooth
import com.app.okra.bluetooth.bluetooth.MultipleBluetoothController
import com.app.okra.bluetooth.callback.*
import com.app.okra.bluetooth.bluetooth.SplitWriter
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.bluetooth.data.BleScanState
import com.app.okra.bluetooth.exception.OtherException
import com.app.okra.bluetooth.scan.BleScanRuleConfig
import com.app.okra.bluetooth.scan.BleScanner
import com.app.okra.bluetooth.utils.BleLog

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleManager {
    private var context: Application? = null

    /**
     * get the ScanRuleConfig
     *
     * @return
     */
    var scanRuleConfig: BleScanRuleConfig? = null
        private set

    /**
     * Get the BluetoothAdapter
     *
     * @return
     */
    var bluetoothAdapter: BluetoothAdapter? = null
        private set

    /**
     * Get the multiple Bluetooth Controller
     *
     * @return
     */
    var multipleBluetoothController: MultipleBluetoothController? = null
        private set

    /**
     * Get the BluetoothManager
     *
     * @return
     */
    var bluetoothManager: BluetoothManager? = null
        private set

    /**
     * Get the maximum number of connections
     *
     * @return
     */
    var maxConnectCount = DEFAULT_MAX_MULTIPLE_DEVICE
        private set

    /**
     * Get operate timeout
     *
     * @return
     */
    var operateTimeout = DEFAULT_OPERATE_TIME
        private set

    /**
     * Get connect retry count
     *
     * @return
     */
    var reConnectCount = DEFAULT_CONNECT_RETRY_COUNT
        private set

    /**
     * Get connect retry interval
     *
     * @return
     */
    var reConnectInterval = DEFAULT_CONNECT_RETRY_INTERVAL.toLong()
        private set

    /**
     * Get operate split Write Num
     *
     * @return
     */
    var splitWriteNum = DEFAULT_WRITE_DATA_SPLIT_COUNT
        private set

    /**
     * Get operate connect Over Time
     *
     * @return
     */
    var connectOverTime = DEFAULT_CONNECT_OVER_TIME.toLong()
        private set

    private object BleManagerHolder {
     val sBleManager: BleManager = BleManager()
    }

    fun init(app: Application?) {
        if (context == null && app != null) {
            context = app
            if (isSupportBle) {
                bluetoothManager =
                    context!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            }
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            multipleBluetoothController = MultipleBluetoothController()
            scanRuleConfig = BleScanRuleConfig()
        }
    }

    /**
     * Get the Context
     *
     * @return
     */
    fun getContext(): Context? {
        return context
    }

    /**
     * Configure scan and connection properties
     *
     * @param config
     */
    fun initScanRule(config: BleScanRuleConfig?) {
        scanRuleConfig = config
    }

    /**
     * Set the maximum number of connections
     *
     * @param count
     * @return BleManager
     */
    fun setMaxConnectCount(count: Int): BleManager {
        var _count = count
        if (_count > DEFAULT_MAX_MULTIPLE_DEVICE) _count = DEFAULT_MAX_MULTIPLE_DEVICE
        maxConnectCount = count
        return this
    }

    /**
     * Set operate timeout
     *
     * @param count
     * @return BleManager
     */
    fun setOperateTimeout(count: Int): BleManager {
        operateTimeout = count
        return this
    }

    /**
     * Set connect retry count and interval
     *
     * @param count
     * @return BleManager
     */
    fun setReConnectCount(count: Int): BleManager {
        return setReConnectCount(count, DEFAULT_CONNECT_RETRY_INTERVAL.toLong())
    }

    /**
     * Set connect retry count and interval
     *
     * @param count
     * @return BleManager
     */
    fun setReConnectCount(count: Int, interval: Long): BleManager {
        var count = count
        var interval = interval
        if (count > 10) count = 10
        if (interval < 0) interval = 0
        reConnectCount = count
        reConnectInterval = interval
        return this
    }

    /**
     * Set split Writ eNum
     *
     * @param num
     * @return BleManager
     */
    fun setSplitWriteNum(num: Int): BleManager {
        if (num > 0) {
            splitWriteNum = num
        }
        return this
    }

    /**
     * Set connect Over Time
     *
     * @param time
     * @return BleManager
     */
    fun setConnectOverTime(time: Long): BleManager {
        var time = time
        if (time <= 0) {
            time = 100
        }
        connectOverTime = time
        return this
    }

    /**
     * print log?
     *
     * @param enable
     * @return BleManager
     */
    fun enableLog(enable: Boolean): BleManager {
        BleLog.isPrint = enable
        return this
    }

    /**
     * scan device around
     *
     * @param callback
     */
    fun scan(callback: BleScanCallback?) {
        requireNotNull(callback) { "BleScanCallback can not be Null!" }
        if (!isBlueEnable) {
            BleLog.e("Bluetooth not enable!")
            callback.onScanStarted(false)
            return
        }
        scanRuleConfig?.let {
            val serviceUuids = it.serviceUuids
            val deviceNames = it.deviceNames
            val deviceMac = it.deviceMac
            val fuzzy = it.isFuzzy
            val timeOut =it.scanTimeOut
            BleScanner.instance
                .scan(serviceUuids, deviceNames, deviceMac, fuzzy, timeOut, callback)
        }
    }

    /**
     * scan device then connect
     *
     * @param callback
     */
    fun scanAndConnect(callback: BleScanAndConnectCallback?) {
        requireNotNull(callback) { "BleScanAndConnectCallback can not be Null!" }
        if (!isBlueEnable) {
            BleLog.e("Bluetooth not enable!")
            callback.onScanStarted(false)
            return
        }
        val serviceUuids = scanRuleConfig!!.serviceUuids
        val deviceNames = scanRuleConfig!!.deviceNames
        val deviceMac = scanRuleConfig!!.deviceMac
        val fuzzy = scanRuleConfig!!.isFuzzy
        val timeOut = scanRuleConfig!!.scanTimeOut
        BleScanner.instance
            .scanAndConnect(serviceUuids!!, deviceNames!!, deviceMac!!, fuzzy, timeOut, callback)
    }

    /**
     * connect a known device
     *
     * @param bleDevice
     * @param bleGattCallback
     * @return
     */
    fun connect(bleDevice: BleDevice?, bleGattCallback: BleGattCallback?): BluetoothGatt? {
        requireNotNull(bleGattCallback) { "BleGattCallback can not be Null!" }
        if (!isBlueEnable) {
            BleLog.e("Bluetooth not enable!")
            bleGattCallback.onConnectFail(bleDevice, OtherException("Bluetooth not enable!"))
            return null
        }
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            BleLog.w("Be careful: currentThread is not MainThread!")
        }
        if (bleDevice?.device == null) {
            bleGattCallback.onConnectFail(
                bleDevice,
                OtherException("Not Found Device Exception Occurred!")
            )
        } else {
            val bleBluetooth = multipleBluetoothController!!.buildConnectingBle(bleDevice)
            val autoConnect = scanRuleConfig!!.isAutoConnect
            return bleBluetooth!!.connect(bleDevice, autoConnect, bleGattCallback)
        }
        return null
    }

    /**
     * connect a device through its mac without scan,whether or not it has been connected
     *
     * @param mac
     * @param bleGattCallback
     * @return
     */
    fun connect(mac: String?, bleGattCallback: BleGattCallback?): BluetoothGatt? {
        val bluetoothDevice = bluetoothAdapter!!.getRemoteDevice(mac)
        val bleDevice = BleDevice(bluetoothDevice, 0, null, 0)
        return connect(bleDevice, bleGattCallback)
    }

    /**
     * Cancel scan
     */
    fun cancelScan() {
        BleScanner.instance.stopLeScan()
    }

    /**
     * notify
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_notify
     * @param callback
     */
    fun notify(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_notify: String?,
        callback: BleNotifyCallback?
    ) {
        notify(bleDevice, uuid_service, uuid_notify, false, callback)
    }

    /**
     * notify
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_notify
     * @param useCharacteristicDescriptor
     * @param callback
     */
    fun notify(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_notify: String?,
        useCharacteristicDescriptor: Boolean,
        callback: BleNotifyCallback?
    ) {
        requireNotNull(callback) { "BleNotifyCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onNotifyFailure(OtherException("This device not connect!"))
        } else {
            bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_notify)
                .enableCharacteristicNotify(callback, uuid_notify!!, useCharacteristicDescriptor)
        }
    }

    /**
     * indicate
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_indicate
     * @param callback
     */
    fun indicate(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_indicate: String?,
        callback: BleIndicateCallback?
    ) {
        indicate(bleDevice, uuid_service, uuid_indicate, false, callback)
    }

    /**
     * indicate
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_indicate
     * @param useCharacteristicDescriptor
     * @param callback
     */
    fun indicate(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_indicate: String?,
        useCharacteristicDescriptor: Boolean,
        callback: BleIndicateCallback?
    ) {
        requireNotNull(callback) { "BleIndicateCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onIndicateFailure(OtherException("This device not connect!"))
        } else {
            bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_indicate)
                .enableCharacteristicIndicate(callback, uuid_indicate!!, useCharacteristicDescriptor)
        }
    }
    /**
     * stop notify, remove callback
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_notify
     * @param useCharacteristicDescriptor
     * @return
     */
    /**
     * stop notify, remove callback
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_notify
     * @return
     */
    @JvmOverloads
    fun stopNotify(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_notify: String?,
        useCharacteristicDescriptor: Boolean = false
    ): Boolean {
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice) ?: return false
        val success = bleBluetooth.newBleConnector()
            .withUUIDString(uuid_service, uuid_notify)
            .disableCharacteristicNotify(useCharacteristicDescriptor)
        if (success) {
            bleBluetooth.removeNotifyCallback(uuid_notify!!)
        }
        return success
    }
    /**
     * stop indicate, remove callback
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_indicate
     * @param useCharacteristicDescriptor
     * @return
     */
    /**
     * stop indicate, remove callback
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_indicate
     * @return
     */
    @JvmOverloads
    fun stopIndicate(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_indicate: String?,
        useCharacteristicDescriptor: Boolean = false
    ): Boolean {
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice) ?: return false
        val success = bleBluetooth.newBleConnector()
            .withUUIDString(uuid_service, uuid_indicate)
            .disableCharacteristicIndicate(useCharacteristicDescriptor)
        if (success) {
            bleBluetooth.removeIndicateCallback(uuid_indicate!!)
        }
        return success
    }

    /**
     * write
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_write
     * @param data
     * @param callback
     */
    fun write(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_write: String?,
        data: ByteArray?,
        callback: BleWriteCallback?
    ) {
        write(bleDevice, uuid_service, uuid_write, data, true, callback)
    }

    /**
     * write
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_write
     * @param data
     * @param split
     * @param callback
     */
    fun write(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_write: String?,
        data: ByteArray?,
        split: Boolean,
        callback: BleWriteCallback?
    ) {
        write(bleDevice, uuid_service, uuid_write, data, split, true, 0, callback)
    }

    /**
     * write
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_write
     * @param data
     * @param split
     * @param sendNextWhenLastSuccess
     * @param intervalBetweenTwoPackage
     * @param callback
     */
    fun write(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_write: String?,
        data: ByteArray?,
        split: Boolean,
        sendNextWhenLastSuccess: Boolean,
        intervalBetweenTwoPackage: Long,
        callback: BleWriteCallback?
    ) {
        requireNotNull(callback) { "BleWriteCallback can not be Null!" }
        if (data == null) {
            BleLog.e("data is Null!")
            callback.onWriteFailure(OtherException("data is Null!"))
            return
        }
        if (data.size > 20 && !split) {
            BleLog.w("Be careful: data's length beyond 20! Ensure MTU higher than 23, or use spilt write!")
        }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onWriteFailure(OtherException("This device not connect!"))
        } else {
            if (split && data.size > splitWriteNum) {
                SplitWriter().splitWrite(
                    bleBluetooth, uuid_service, uuid_write, data,
                    sendNextWhenLastSuccess, intervalBetweenTwoPackage, callback
                )
            } else {
                bleBluetooth.newBleConnector()
                    .withUUIDString(uuid_service, uuid_write)
                    .writeCharacteristic(data, callback, uuid_write!!)
            }
        }
    }

    /**
     * read
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_read
     * @param callback
     */
    fun read(
        bleDevice: BleDevice?,
        uuid_service: String?,
        uuid_read: String?,
        callback: BleReadCallback?
    ) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_read)
                .readCharacteristic(callback, uuid_read!!)
        }
    }

    /**
     * read Rssi
     *
     * @param bleDevice
     * @param callback
     */
    fun readRssi(
        bleDevice: BleDevice?,
        callback: BleRssiCallback?
    ) {
        requireNotNull(callback) { "BleRssiCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onRssiFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector().readRemoteRssi(callback)
        }
    }

    /**
     * set Mtu
     *
     * @param bleDevice
     * @param mtu
     * @param callback
     */
    fun setMtu(
        bleDevice: BleDevice?,
        mtu: Int,
        callback: BleMtuChangedCallback?
    ) {
        requireNotNull(callback) { "BleMtuChangedCallback can not be Null!" }
        if (mtu > DEFAULT_MAX_MTU) {
            BleLog.e("requiredMtu should lower than 512 !")
            callback.onSetMTUFailure(OtherException("requiredMtu should lower than 512 !"))
            return
        }
        if (mtu < DEFAULT_MTU) {
            BleLog.e("requiredMtu should higher than 23 !")
            callback.onSetMTUFailure(OtherException("requiredMtu should higher than 23 !"))
            return
        }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onSetMTUFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector().setMtu(mtu, callback)
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
    fun requestConnectionPriority(bleDevice: BleDevice?, connectionPriority: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
            return bleBluetooth?.newBleConnector()?.requestConnectionPriority(connectionPriority)
                ?: false
        }
        return false
    }

    /**
     * is support ble?
     *
     * @return
     */
    val isSupportBle: Boolean
        get() = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context!!.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))

    /**
     * Open bluetooth
     */
    fun enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter!!.enable()
        }
    }

    /**
     * Disable bluetooth
     */
    fun disableBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter!!.isEnabled) bluetoothAdapter!!.disable()
        }
    }

    /**
     * judge Bluetooth is enable
     *
     * @return
     */
    val isBlueEnable: Boolean
        get() = bluetoothAdapter != null && bluetoothAdapter!!.isEnabled

    fun convertBleDevice(bluetoothDevice: BluetoothDevice?): BleDevice {
        return BleDevice(bluetoothDevice)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun convertBleDevice(scanResult: ScanResult?): BleDevice {
        requireNotNull(scanResult) { "scanResult can not be Null!" }
        val bluetoothDevice = scanResult.device
        val rssi = scanResult.rssi
        val scanRecord = scanResult.scanRecord
        var bytes: ByteArray? = null
        if (scanRecord != null) bytes = scanRecord.bytes
        val timestampNanos = scanResult.timestampNanos
        return BleDevice(bluetoothDevice, rssi, bytes, timestampNanos)
    }

    fun getBleBluetooth(bleDevice: BleDevice?): BleBluetooth? {
        return if (multipleBluetoothController != null) {
            multipleBluetoothController!!.getBleBluetooth(bleDevice)
        } else null
    }

    fun getBluetoothGatt(bleDevice: BleDevice?): BluetoothGatt? {
        val bleBluetooth = getBleBluetooth(bleDevice)
        return bleBluetooth?.bluetoothGatt
    }

    fun getBluetoothGattServices(bleDevice: BleDevice?): List<BluetoothGattService>? {
        val gatt = getBluetoothGatt(bleDevice)
        return gatt?.services
    }

    fun getBluetoothGattCharacteristics(service: BluetoothGattService): List<BluetoothGattCharacteristic> {
        return service.characteristics
    }

    fun removeConnectGattCallback(bleDevice: BleDevice?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeConnectGattCallback()
    }

    fun removeRssiCallback(bleDevice: BleDevice?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeRssiCallback()
    }

    fun removeMtuChangedCallback(bleDevice: BleDevice?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeMtuChangedCallback()
    }

    fun removeNotifyCallback(bleDevice: BleDevice?, uuid_notify: String?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeNotifyCallback(uuid_notify!!)
    }

    fun removeIndicateCallback(bleDevice: BleDevice?, uuid_indicate: String?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeIndicateCallback(uuid_indicate!!)
    }

    fun removeWriteCallback(bleDevice: BleDevice?, uuid_write: String?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeWriteCallback(uuid_write!!)
    }

    fun removeReadCallback(bleDevice: BleDevice?, uuid_read: String?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeReadCallback(uuid_read!!)
    }

    fun clearCharacterCallback(bleDevice: BleDevice?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.clearCharacterCallback()
    }

    val scanSate: BleScanState
        get() = BleScanner.instance.scanState
    val allConnectedDevice: List<BleDevice>?
        get() = if (multipleBluetoothController == null) null else multipleBluetoothController!!.deviceList

    /**
     * @param bleDevice
     * @return State of the profile connection. One of
     * [BluetoothProfile.STATE_CONNECTED],
     * [BluetoothProfile.STATE_CONNECTING],
     * [BluetoothProfile.STATE_DISCONNECTED],
     * [BluetoothProfile.STATE_DISCONNECTING]
     */
    fun getConnectState(bleDevice: BleDevice?): Int {
        return if (bleDevice != null) {
            bluetoothManager!!.getConnectionState(bleDevice.device, BluetoothProfile.GATT)
        } else {
            BluetoothProfile.STATE_DISCONNECTED
        }
    }

    fun isConnected(bleDevice: BleDevice?): Boolean {
        return getConnectState(bleDevice) == BluetoothProfile.STATE_CONNECTED
    }

    fun isConnected(mac: String): Boolean {
        val list = allConnectedDevice
        for (bleDevice in list!!) {
            if (bleDevice != null) {
                if (bleDevice.mac == mac) {
                    return true
                }
            }
        }
        return false
    }

    fun disconnect(bleDevice: BleDevice?) {
        if (multipleBluetoothController != null) {
            multipleBluetoothController!!.disconnect(bleDevice)
        }
    }

    fun disconnectAllDevice() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController!!.disconnectAllDevice()
        }
    }

    fun destroy() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController!!.destroy()
        }
    }




    companion object {
        const val DEFAULT_SCAN_TIME = 10000
        private const val DEFAULT_MAX_MULTIPLE_DEVICE = 7
        private const val DEFAULT_OPERATE_TIME = 5000
        private const val DEFAULT_CONNECT_RETRY_COUNT = 0
        private const val DEFAULT_CONNECT_RETRY_INTERVAL = 5000
        private const val DEFAULT_MTU = 23
        private const val DEFAULT_MAX_MTU = 512
        private const val DEFAULT_WRITE_DATA_SPLIT_COUNT = 20
        private const val DEFAULT_CONNECT_OVER_TIME = 10000

       val instance = BleManagerHolder.sBleManager



    }


}