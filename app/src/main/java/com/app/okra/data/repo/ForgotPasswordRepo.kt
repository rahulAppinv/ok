package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.ForgotPasswordRequest


interface ForgotPasswordRepo {
    suspend fun executeForgotPasswordRequest(request : ForgotPasswordRequest)
            : ApiResult<ApiData<Any>>
}