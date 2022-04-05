package com.app.okra.amazonS3


import android.net.Uri
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver

class ImageBean {
    var name: String? = null
    var progress = 0
    var mObserver: TransferObserver? = null
    var serverUrl = ""
    var isSuccess = "0"
    var imagePath: String = ""
    var id: Int? = null
    var uri: Uri? = null


}
