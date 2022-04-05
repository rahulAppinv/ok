package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.ResetPasswordRequest


interface ResetPasswordRepo {
    suspend fun executeResetPasswordRequest(request : ResetPasswordRequest)
            : ApiResult<ApiData<Any>>

    suspend fun changePasswordRequest(request : ResetPasswordRequest)
            : ApiResult<ApiData<Any>>

}