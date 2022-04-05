package com.app.okra.bluetooth.callback

import android.os.Handler

abstract class BleBaseCallback {
    var key: String? = null
    var handler: Handler? = null
}