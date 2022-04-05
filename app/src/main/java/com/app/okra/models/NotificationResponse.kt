package com.app.okra.models

class NotificationRequest {
        var notificationId: ArrayList<String>?=null
        var type: String?=null
}

class NotificationResponse(
        var today: ArrayList<Notification>?= null,
        var earlier: ArrayList<Notification>?= null
)

class Notification(
        var _id: String?= null,
        var type: String?= null,
        var message: String?= null,
        var body: String?= null,
        var title: String?= null,
        var senderId: String?= null,
        var createdAt: String?= null,
        var created: Long?= null,
        var isRead: Boolean?= null,
        var receiverId: ArrayList<String>?= null
)