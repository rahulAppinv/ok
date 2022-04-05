package com.app.okra.ui.logbook.test.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.app.okra.models.Data
import com.app.okra.ui.logbook.test.TestDetailsActivity
import com.app.okra.utils.AppConstants.RequestOrResultCodes.RESULT_CODE_TEST_LOG_DELETED
import com.app.okra.utils.AppConstants.RequestOrResultCodes.RESULT_CODE_TEST_LOG_UPDATED

class TestLogContract :ActivityResultContract<Data, Boolean>() {

    override fun createIntent(context: Context, input: Data?): Intent {
        return Intent(context, TestDetailsActivity::class.java).putExtra("data", input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == RESULT_CODE_TEST_LOG_DELETED || resultCode == RESULT_CODE_TEST_LOG_UPDATED
    }
}