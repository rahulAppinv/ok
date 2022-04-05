package com.app.okra.ui.logbook.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.app.okra.models.MealData
import com.app.okra.ui.logbook.meal.MealDetailsActivity
import com.app.okra.utils.AppConstants.RequestOrResultCodes.RESULT_CODE_MEAL_LOG_DELETED
import com.app.okra.utils.AppConstants.RequestOrResultCodes.RESULT_CODE_MEAL_LOG_UPDATED

class MealLogContract :ActivityResultContract<MealData, Boolean>() {

    override fun createIntent(context: Context, input: MealData?): Intent {
        return Intent(context, MealDetailsActivity::class.java).putExtra("data", input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == RESULT_CODE_MEAL_LOG_DELETED || resultCode == RESULT_CODE_MEAL_LOG_UPDATED
    }
}