package com.app.okra.data.network


class ApiData<out T> (
        val data: T? = null,
        val statusCode: String?=null,
        val message: String?=null,
        val type: String?=null,
        val error: String?=null,
        val total: Int? =0,
        val pageNo: Int? =0,
        val totalPage: Int?=0,
        val nextHit: Int?=0,
        val limit: Int?=0,
)