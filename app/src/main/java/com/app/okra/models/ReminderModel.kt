package com.app.okra.models

class ReminderRequest {
    var reminderType: Int?=null
    var startDate: String?=null
    var endDate: String?=null
    var time: String?=null
    var repeatType: String?=null
    var endRepeatType: String?=null
}

class ReminderResponse {
     var contact: String?=null
    var email: String?=null
    var address: String?=null
}