package com.app.okra.models

data class ItemModel(val name :String, val icon :Int)

class UserDetailResponse(){
    var userId: String?=null
    var email: String?=null
    var userType: String?=null
    var name: String?=null
    var age: String?=null
    var mobile: String?=null
    var foodReminder: FoodReminder?=null
    var diabetesReminder: FoodReminder? = null
    var medicationReminder: FoodReminder? = null
}

class FoodReminder(){
    var endDate: String?=null
    var endRepeatType: String?=null
    var reminderType: Int?=null
    var repeatType: String?=null
    var startDate: String?=null
    var time: String?=null
}