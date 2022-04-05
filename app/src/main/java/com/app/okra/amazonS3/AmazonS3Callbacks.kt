package com.app.okra.amazonS3


interface AmazonS3Callbacks {
    fun uploadError(e: Exception, imageBean: ImageBean)
    fun uploadProgress(imageBean: ImageBean)
    fun uploadSuccess(imageBean: ImageBean)
    fun uploadFailed(imageBean: ImageBean)

}