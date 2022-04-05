package com.app.okra.utils

import android.content.BroadcastReceiver
import android.app.NotificationManager
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null) {
            if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true)) {
                return
            }
        }
        //Trigger the notification
        displayNotification(context, intent)
    }
}