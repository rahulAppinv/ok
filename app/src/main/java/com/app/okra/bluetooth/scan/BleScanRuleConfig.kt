package com.app.okra.bluetooth.scan

import com.app.okra.bluetooth.BleManager
import java.util.*

class BleScanRuleConfig {
    var serviceUuids: Array<UUID>? = null
        private set
    var deviceNames: Array<String>? = null
        private set
    var deviceMac: String? = null
        private set
    var isAutoConnect = false
        private set
    var isFuzzy = false
        private set
    var scanTimeOut = BleManager.DEFAULT_SCAN_TIME.toLong()
        private set

    class Builder {
        private var mServiceUuids: Array<UUID>? = null
        private var mDeviceNames: Array<String>? = null
        private var mDeviceMac: String? = null
        private var mAutoConnect = false
        private var mFuzzy = false
        private var mTimeOut = BleManager.DEFAULT_SCAN_TIME.toLong()
        fun setServiceUuids(uuids: Array<UUID>?): Builder {
            mServiceUuids = uuids
            return this
        }

       /* fun setDeviceName(fuzzy: Boolean, vararg name: String): Builder {
            mFuzzy = fuzzy
            mDeviceNames = name
            return this
        }*/

        fun setDeviceMac(mac: String?): Builder {
            mDeviceMac = mac
            return this
        }

        fun setAutoConnect(autoConnect: Boolean): Builder {
            mAutoConnect = autoConnect
            return this
        }

        fun setScanTimeOut(timeOut: Long): Builder {
            mTimeOut = timeOut
            return this
        }

        fun applyConfig(config: BleScanRuleConfig) {
            config.serviceUuids = mServiceUuids
            config.deviceNames = mDeviceNames
            config.deviceMac = mDeviceMac
            config.isAutoConnect = mAutoConnect
            config.isFuzzy = mFuzzy
            config.scanTimeOut = mTimeOut
        }

        fun build(): BleScanRuleConfig {
            val config = BleScanRuleConfig()
            applyConfig(config)
            return config
        }
    }
}