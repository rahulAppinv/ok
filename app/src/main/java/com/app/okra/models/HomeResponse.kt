package com.app.okra.models

class HomeResponse(
        var graphInfo: ArrayList<GraphInfo>?= null,
        var totalTest: String?=null,
        var avgBloodGlucose: Double?=null,
        var avgInsulin: Double?=null,
        var hyper_hypes: HyperHypes?=null,
        var Est_HbA1c: Double?=null,
        var carbsCount: Double?=null,
        var foodLogs: ArrayList<MealData>?=null,
        var userInfo: UserInfo?=null
        )

class GraphInfo(
        var _id: Int?=null,
        var bloodGlucose: String?=null,
        var hours: String?=null,
        var day: String?=null
)
class UserInfo(
        var _id: String?=null,
        var isApproved: Boolean?=null,
        var isVerify: Boolean?=null,
)

class HyperHypes(
        var hyper: String?=null,
        var hypos: String?=null
)