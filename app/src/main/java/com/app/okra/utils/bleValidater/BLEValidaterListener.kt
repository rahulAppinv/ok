package com.app.okra.utils.bleValidater

import com.app.okra.utils.PermissionUtils

interface BLEValidaterListener : PermissionUtils.IGetPermissionListener {

    fun onBluetoothDisable(msg: String)
    fun onLocationDisable(msg: String)
}