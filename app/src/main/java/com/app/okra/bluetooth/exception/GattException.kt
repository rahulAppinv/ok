package com.app.okra.bluetooth.exception

class GattException(var gattStatus: Int) :
    BleException(ERROR_CODE_GATT, "Gatt Exception Occurred! ") {
    fun setGattStatus(gattStatus: Int): GattException {
        this.gattStatus = gattStatus
        return this
    }

    override fun toString(): String {
        return "GattException{" +
                "gattStatus=" + gattStatus +
                "} " + super.toString()
    }
}