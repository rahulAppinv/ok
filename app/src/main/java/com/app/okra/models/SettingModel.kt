package com.app.okra.models

class SettingRequest {

    var pushNotificationStatus: Boolean?=null
    var inappNotificationStatus: Boolean?=null
    var bloodGlucoseUnit: String?=null
    var hyperBloodGlucoseValue: String?=null
    var hypoBloodGlucoseValue: String?=null

}

class ContactResponse {
     var contact: String?=null
    var email: String?=null
    var address: String?=null
}