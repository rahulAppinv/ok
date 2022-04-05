package com.app.okra.amazonS3

import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.app.sensor.amazonS3.AmazonS3Callbacks
import java.io.File
import java.util.*


class AmazonS3 {
    private var mActivity: Activity? = null
    private var amazonCallback: AmazonS3Callbacks? = null
    private var mTransferUtility: TransferUtility? = null


    /**
     * Initializes Amazon S3
     * @param activity          Activity where callbacks are to sent
     * @param amazonCallback    Interface through which actions are implemented on the required activity
     */
    fun setCallback(activity: Activity, amazonCallback: AmazonS3Callbacks) {
        this.mActivity = activity
        this.amazonCallback = amazonCallback
    }


    /**
     * Uploads image to S3
     */
    fun uploadFile(imageBean: ImageBean) {
        val file = File(imageBean.uri?.path!!)
        if (file.exists()) {
            mTransferUtility = AmazonS3Utils.getTransferUtility(mActivity!!)
            val observer = mTransferUtility?.upload(
                AmazonS3Constants.BUCKET,
                file.name + Calendar.getInstance().timeInMillis.toString() + "." + getFileExtension(
                    imageBean.uri ?: Uri.fromFile(file)
                ),
                file,
                CannedAccessControlList.PublicRead
            )
            observer?.setTransferListener(UploadListener(imageBean))
            imageBean.mObserver = (observer)

        }else{
            amazonCallback?.uploadError(java.lang.Exception("No file found"), imageBean)
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver: ContentResolver = mActivity?.contentResolver!!
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }


    private inner class UploadListener(private val imageBean: ImageBean) : TransferListener {

        // Simply updates the UI list when notified.
        override fun onError(id: Int, e: Exception) {
            imageBean.isSuccess = "0"
            amazonCallback?.uploadError(e, imageBean)
        }

        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            val progress = (bytesCurrent.toDouble() * 100 / bytesTotal).toInt()
            imageBean.progress = (progress)
            amazonCallback?.uploadProgress(imageBean)
        }

        override fun onStateChanged(id: Int, newState: TransferState) {
            if (newState == TransferState.COMPLETED) {
                imageBean.isSuccess = ("1")
                val url = AmazonS3Constants.AMAZON_SERVER_URL + imageBean.mObserver?.key
                imageBean.serverUrl = (url)
                amazonCallback?.uploadSuccess(imageBean)
            } else if (newState == TransferState.FAILED) {
                imageBean.isSuccess = ("0")
                amazonCallback?.uploadFailed(imageBean)
            }
        }
    }

    fun removeCallBack() {
        amazonCallback = null
    }
}