package com.app.okra.ui.add_meal.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.app.okra.models.Items
import com.app.okra.ui.add_meal.ImageViewActivity
import com.app.okra.ui.add_meal.MealInput
import com.app.okra.utils.AppConstants
import com.app.okra.utils.AppConstants.RequestOrResultCodes.MEAL_ADDED

class AddMealContracts :ActivityResultContract<MealInput, Items?>() {

    companion object {
         val data = AppConstants.DATA
         val noOfServing = AppConstants.NO_OF_SERVING
    }

    override fun createIntent(context: Context, mealInput: MealInput): Intent {
        return Intent(context, ImageViewActivity::class.java)
            .putExtra(data, mealInput)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Items?{
        return if(resultCode == AppCompatActivity.RESULT_OK && intent!=null){
                intent.getParcelableExtra(data)
        }else null
    }

}