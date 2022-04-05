package com.app.sensor.amazonS3

import com.app.okra.amazonS3.ImageBean


interface AmazonS3Callbacks {
    fun uploadError(e: Exception, imageBean: ImageBean)
    fun uploadProgress(imageBean: ImageBean)
    fun uploadSuccess(imageBean: ImageBean)
    fun uploadFailed(imageBean: ImageBean)

}