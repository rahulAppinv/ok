package com.app.okra.utils.bleValidater

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.location.LocationManager
import android.os.Build
import com.app.okra.R
import com.app.okra.utils.PermissionUtils

class BleValidate(private val context: Context, private var listener: BLEValidaterListener?= null)
    :PermissionUtils.IGetPermissionListener{

    private val mPermissionUtils = PermissionUtils(this)

     fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            listener?.onBluetoothDisable("")
            return
        }

        if(mPermissionUtils.checkAndGetLocationPermissions(context)){
            listener?.onPermissionsGiven(-1)
        }
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onPermissionsGiven(data: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if(!checkGPSIsOpen()) {
                listener?.onLocationDisable("")
            }else{
                listener?.onPermissionsGiven(data)
            }
        }else{
            listener?.onPermissionsGiven(data)
        }

    }

    override fun onPermissionsDeny(data: Int) {
        listener?.onPermissionsDeny(data)
    }



}