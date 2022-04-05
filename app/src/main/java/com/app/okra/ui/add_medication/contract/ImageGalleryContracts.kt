package com.app.okra.ui.add_medication.contract

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.app.okra.utils.AppConstants

class ImageGalleryContracts:ActivityResultContract<Void, Intent?>() {

    companion object {
         val data = AppConstants.DATA
    }

    override fun createIntent(context: Context, input: Void): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent?{
        return if(resultCode == AppCompatActivity.RESULT_OK && intent!=null){
            intent!!
        }else null
    }


}