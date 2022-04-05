package com.app.okra.bluetooth.data

import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable

class BleDevice() : Parcelable {
    var device: BluetoothDevice? = null
    var scanRecord: ByteArray? = null
    var rssi = 0
    var timestampNanos: Long = 0
    val isPaired: Boolean = false

    var localName: String? = null
    var localDeviceId: String? = null


    constructor(device: BluetoothDevice?) : this() {
        this.device = device
    }

    constructor(
        device: BluetoothDevice?,
        rssi: Int,
        scanRecord: ByteArray?,
        timestampNanos: Long
    ) : this() {
        this.device = device
        this.scanRecord = scanRecord
        this.rssi = rssi
        this.timestampNanos = timestampNanos
    }

    protected constructor(`in`: Parcel) : this() {
        device = `in`.readParcelable(BluetoothDevice::class.java.classLoader)
        scanRecord = `in`.createByteArray()
        rssi = `in`.readInt()
        timestampNanos = `in`.readLong()
        localName = `in`.readString()
        localDeviceId = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(device, flags)
        dest.writeByteArray(scanRecord)
        dest.writeInt(rssi)
        dest.writeLong(timestampNanos)
        dest.writeString(localName)
        dest.writeString(localDeviceId)
    }

    override fun describeContents(): Int {
        return 0
    }

    val name: String?
        get() = if (device != null) device!!.name else localName
    val mac: String?
        get() = if (device != null) device!!.address else localDeviceId
    val deviceKey: String
        get() = gettingDeviceKey()

    private fun gettingDeviceKey(): String {
        var completeName = ""
        completeName = when {
            device != null -> {
                device!!.name
            }
            localName != null -> {
                localName!!
            }
            else -> {
                ""
            }
        }

        completeName += when {
            device != null -> {
                device!!.address
            }
            localDeviceId != null -> {
                localDeviceId!!
            }
            else -> {
                ""
            }
        }
        return completeName
    }

    companion object CREATOR : Parcelable.Creator<BleDevice> {
        override fun createFromParcel(parcel: Parcel): BleDevice {
            return BleDevice(parcel)
        }

        override fun newArray(size: Int): Array<BleDevice?> {
            return arrayOfNulls(size)
        }
    }
}