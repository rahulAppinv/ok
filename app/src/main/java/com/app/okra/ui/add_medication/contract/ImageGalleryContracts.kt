package com.app.okra.ui.add_medication.contract

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.app.okra.models.Items
import com.app.okra.ui.add_meal.ImageViewActivity
import com.app.okra.ui.add_meal.MealInput
import com.app.okra.utils.AppConstants
import com.app.okra.utils.AppConstants.RequestOrResultCodes.MEAL_ADDED
import java.io.File
import java.io.IOException

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