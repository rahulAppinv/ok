package com.app.okra.utils.bleValidater

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity

class GPSContract :ActivityResultContract<Void, Boolean>() {

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == AppCompatActivity.RESULT_OK && intent!=null
    }

    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }
}