package com.app.okra.data.network


class BaseResponse<T>(
    val data: T?,
    val statusCode: String?,
    val message: String?,
    val type: String?,
    val error: String?, // for internal use
    val total: Int?=0,
    val pageNo: Int,
    val totalPage: Int,
    val nextHit: Int,
    val limit: Int,
    val totalAttendees: Int=0,
)