package com.app.okra.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.content.ContextCompat
import com.app.okra.R
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.utils.AppConstants
import com.app.okra.utils.AppConstants.NotificationConstants.Companion.ADMIN_USER_ACCOUNT_VERIFY
import com.app.okra.utils.sendNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val data: Map<String, String> = p0.data

        val body :String? = data["body"].toString()
        println("::: Hi Hah ha ha h ah ha ha h ah ah ah a ha ah} :${body}")

        if(!body.isNullOrEmpty()) {
            val jsonObject  = Gson().fromJson(body, JsonObject::class.java)

            if(jsonObject.has("type") &&  jsonObject.get("type").asString== ADMIN_USER_ACCOUNT_VERIFY ) {
                println("::: Account Verified")

                PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_VERIFIED, true)
            }else {
                val notificationManager = ContextCompat.getSystemService(
                    this,
                    NotificationManager::class.java
                ) as NotificationManager

                notificationManager.sendNotification(
                    body,
                    applicationContext
                )
            }
        }

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        println("::: Token: $p0")
        PreferenceManager.putString(AppConstants.Pref_Key.DEVICE_TOKEN, p0)
    }

}