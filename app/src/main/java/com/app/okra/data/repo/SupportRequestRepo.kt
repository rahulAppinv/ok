package com.app.okra.data.repo

import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiData
import com.app.okra.models.*
import java.util.*


interface SupportRequestRepo {

    suspend fun getSupportRequestList(params: WeakHashMap<String, Any>)
            : ApiResult<ApiData<List<SupportResponse>>>
    suspend fun sendSupportRequest(params: WeakHashMap<String, Any>)
            : ApiResult<ApiData<Any>>

}