package com.app.okra.utils


import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.gson.Gson
import com.google.gson.JsonObject


// TODO: Step 1.1 extension function to send messages (GIVEN)
/**
 *
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(data: String, applicationContext: Context) {

    val jsonObject  = Gson().fromJson(data, JsonObject::class.java)

    if(jsonObject.has("type") &&
            jsonObject.has("title") &&
            jsonObject.has("message")
    ) {

/*
        val intent :Intent? = getIntentToNavigate( applicationContext, jsonObject)


        // TODO: Step 1.12 create PendingIntent
        var contentPendingIntent :PendingIntent?=null
        intent?.apply {
            contentPendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    NOTIFICATION_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            NOTIFICATION_ID++
        }


        createNotificationChannel(applicationContext)

        // TODO: Step 2.0 add style
        *//*val eggImage = BitmapFactory.decodeResource(
                applicationContext.resources,
                R.mipmap.group
        )*//*
        val bigTextStyle = NotificationCompat.BigTextStyle()
                .bigText(jsonObject.get("message").asString)

        // TODO: Step 1.2 get an instance of NotificationCompat.Builder
        val builder = NotificationCompat.Builder(
                applicationContext,
                applicationContext.getString(R.string.channel_id)
        )

                // TODO: Step 1.3 set title, text and icon to builder
                .setSmallIcon(R.mipmap.group)
                .setContentTitle(jsonObject.get("title").asString)
                .setContentText(jsonObject.get("message").asString)
                .setStyle(bigTextStyle)
                .setAutoCancel(true)

                // TODO: Step 2.1 add style to builder
                .setStyle(bigTextStyle)
                .setLargeIcon(eggImage)

                // TODO: Step 2.5 set priority
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        // TODO: Step 1.13 set content intent
        if(contentPendingIntent!=null)
            builder.setContentIntent(contentPendingIntent)

        // TODO: Step 1.4 call notify
        notify(NOTIFICATION_ID, builder.build())*/
    }


}

fun getIntentToNavigate(applicationContext: Context, jsonObject: JsonObject):Intent? {

    /*jsonObject.apply {
        val type = get("type").asString
        when (type) {
            AppConstants.NotificationConstants.TYPE_CREATE_SHIFT -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                var senderId = ""
                if (jsonObject.has("senderId")) {
                    senderId = jsonObject.get("senderId").asString
                }
                return Intent(applicationContext, AppliedForWorkActivity::class.java).apply {
                    putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                    putExtra(AppConstants.Intent_Constant.TYPE, type)
                    putExtra(AppConstants.Intent_Constant.SENDER_ID, senderId)

                }
            }
            AppConstants.NotificationConstants.TYPE_CREATE_GROUP -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                var senderId = ""
                if (jsonObject.has("senderId")) {
                    senderId = jsonObject.get("senderId").asString
                }
                return Intent(applicationContext, AppliedForWorkActivity::class.java).apply {
                    putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                    putExtra(AppConstants.Intent_Constant.TYPE, type)
                    putExtra(AppConstants.Intent_Constant.SENDER_ID, senderId)
                }
            }
            AppConstants.NotificationConstants.SHIFT_APPLY_FOR_WORK -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                var senderId = ""
                if (jsonObject.has("senderId")) {
                    senderId = jsonObject.get("senderId").asString
                }

                return Intent(applicationContext, AppliedForWorkActivity::class.java).apply {
                    putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                    putExtra(AppConstants.Intent_Constant.SENDER_ID, senderId)
                    putExtra(AppConstants.Intent_Constant.TYPE, type)
                }
            }
            AppConstants.NotificationConstants.GROUP_APPLY_FOR_WORK -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }

                var senderId = ""
                if (jsonObject.has("senderId")) {
                    senderId = jsonObject.get("senderId").asString
                }

                return Intent(applicationContext, AppliedForWorkActivity::class.java).apply {
                    putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                    putExtra(AppConstants.Intent_Constant.SENDER_ID, senderId)
                    putExtra(AppConstants.Intent_Constant.TYPE, type)
                }
            }
            AppConstants.NotificationConstants.FRIEND_REQUEST_SENT -> {
                var senderUserType = ""
                var requestId = ""
                var senderUserName = ""
                var senderUserProfilePic = ""
                var senderId = ""
                if (jsonObject.has("senderUserType")) {
                    senderUserType = jsonObject.get("senderUserType").asString
                }
                if (jsonObject.has("requestId")) {
                    requestId = jsonObject.get("requestId").asString
                }
                if (jsonObject.has("senderUserName")) {
                    senderUserName = jsonObject.get("senderUserName").asString
                }
                if (jsonObject.has("senderUserProfilePic")) {
                    senderUserProfilePic = jsonObject.get("senderUserProfilePic").asString
                }
                if (jsonObject.has("senderId")) {
                    senderId = jsonObject.get("senderId").asString
                }

                return Intent(applicationContext, FriendRequestActivity::class.java).apply {
                    putExtra(AppConstants.Intent_Constant.SENDER_USER_TYPE, senderUserType)
                    putExtra(AppConstants.Intent_Constant.SENDER_USER_NAME, senderUserName)
                    putExtra(AppConstants.Intent_Constant.REQUEST_ID, requestId)
                    putExtra(AppConstants.Intent_Constant.SENDER_ID, senderId)
                    putExtra(
                            AppConstants.Intent_Constant.SENDER_USER_PROFILE_PIC,
                            senderUserProfilePic
                    )
                }
            }
            AppConstants.NotificationConstants.FRIEND_REQUEST_ACCEPT,
            AppConstants.NotificationConstants.FRIEND_REQUEST_DECLINED
            -> {
                var senderUserType = ""
                var senderUserName = ""
                var senderUserProfilePic = ""
                var senderId = ""

                if (jsonObject.has("senderUserType")) {
                    senderUserType = jsonObject.get("senderUserType").asString
                }
                if (jsonObject.has("senderUserName")) {
                    senderUserName = jsonObject.get("senderUserName").asString
                }
                if (jsonObject.has("senderUserProfilePic")) {
                    senderUserProfilePic = jsonObject.get("senderUserProfilePic").asString
                }
                if (jsonObject.has("senderId")) {
                    senderId = jsonObject.get("senderId").asString
                }

                return if(senderUserType == AppConstants.USER_TYPE_SUPPORTER){
                    Intent(applicationContext, UserSupporterDetailActivity::class.java)
                            .putExtra(AppConstants.Intent_Constant.USER_ID, senderId)
                }else{
                    Intent(applicationContext, UserParticipantDetailActivity::class.java)
                            .putExtra(AppConstants.Intent_Constant.USER_ID, senderId)
                }
            }
            AppConstants.NotificationConstants.GROUP_ACTIVITY_STARTED,
            -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                return Intent(applicationContext, GroupActivityDetailsActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                        .putExtra(AppConstants.Intent_Constant.TYPE,type)
            }
            AppConstants.NotificationConstants.SHIFT_ACTIVITY_STARTED
            -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                return Intent(applicationContext, GroupActivityDetailsActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                        .putExtra(AppConstants.Intent_Constant.TYPE,type)
            }
            AppConstants.NotificationConstants.GROUP_ACTIVITY_DECLINED_SINGLE
            -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                return Intent(applicationContext, ReplaceSingleSupporterActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                        .putExtra(AppConstants.Intent_Constant.TYPE,type)
            }
            AppConstants.NotificationConstants.GROUP_ACTIVITY_DECLINED_MULTIPLE
            -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                return Intent(applicationContext, ReplaceMultipleSupporterActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                        .putExtra(AppConstants.Intent_Constant.TYPE,type)
            }
            AppConstants.NotificationConstants.SUPPORTING_RATING,
            AppConstants.NotificationConstants.GROUP_ACTIVITY_FINISHED,
            AppConstants.NotificationConstants.SHIFT_ACTIVITY_FINISHED
            -> {
                var activityId = ""
                var senderId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                if (jsonObject.has("senderId")) {
                    senderId = jsonObject.get("senderId").asString
                }

                return Intent(applicationContext, NotificationListActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                        .putExtra(AppConstants.Intent_Constant.SENDER_ID, senderId)
                        .putExtra(AppConstants.Intent_Constant.TYPE,type)
            }
            AppConstants.NotificationConstants.MAKE_PUBLIC_SHIFT
            -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }
                MessageActivity.screenType = MessageActivity.MAKE_PUBLIC_SHIFT

                return Intent(applicationContext, MessageActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                        .putExtra(AppConstants.Intent_Constant.TYPE,type)
            }
            AppConstants.NotificationConstants.ADD_NOTE
            -> {
                var activityId = ""
                var senderId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }

                if (jsonObject.has("senderId")) {
                    senderId = jsonObject.get("senderId").asString
                }

                return Intent(applicationContext, NotificationListActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                        .putExtra(AppConstants.Intent_Constant.TYPE,type)
                        .putExtra(AppConstants.Intent_Constant.SENDER_ID,senderId)
            }
            AppConstants.NotificationConstants.NOTES_DECLINED
            -> {
                var activityId = ""
                var activityType = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }

                if (jsonObject.has("activityType")) {
                    activityType = jsonObject.get("activityType").asString
                }

                return Intent(applicationContext, MyNotesActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)
                        .putExtra(AppConstants.Intent_Constant.IS_FIRST_SUPPORTER,true)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_TYPE,activityType)
            }
            AppConstants.NotificationConstants.GROUP_EDIT
            -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }

                return Intent(applicationContext, GroupActivityDetailsActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)

            }
            AppConstants.NotificationConstants.EDIT_SHIFT
            -> {
                var activityId = ""
                if (jsonObject.has("activityId")) {
                    activityId = jsonObject.get("activityId").asString
                }

                return Intent(applicationContext, ShiftActivityDetailsActivity::class.java)
                        .putExtra(AppConstants.Intent_Constant.ACTIVITY_ID, activityId)

            }
        }
    }*/
    return null
}

fun createNotificationChannel(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      /*  val channelName = context.getString(R.string.channel_name)
        val channelId = context.getString(R.string.channel_id)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                importance
        )// TODO: Step 2.6 disable badges for this channel
                .apply {
                    setShowBadge(false)
                }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)

        val notificationManager = context.getSystemService(
                NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)*/
    }
}


/*
 * Cancels all notifications.
 */
fun NotificationManager.cancelNotifications() {
    cancelAll()
}