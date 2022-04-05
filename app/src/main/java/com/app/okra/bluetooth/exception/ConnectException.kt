package com.app.okra.bluetooth.exception

import android.bluetooth.BluetoothGatt

class ConnectException(var bluetoothGatt: BluetoothGatt, var gattStatus: Int) : BleException(
    ERROR_CODE_GATT, "Gatt Exception Occurred! "
) {
    fun setGattStatus(gattStatus: Int): ConnectException {
        this.gattStatus = gattStatus
        return this
    }

    fun setBluetoothGatt(bluetoothGatt: BluetoothGatt): ConnectException {
        this.bluetoothGatt = bluetoothGatt
        return this
    }

    override fun toString(): String {
        return "ConnectException{" +
                "gattStatus=" + gattStatus +
                ", bluetoothGatt=" + bluetoothGatt +
                "} " + super.toString()
    }
}