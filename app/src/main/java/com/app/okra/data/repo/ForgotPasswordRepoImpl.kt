package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.ForgotPasswordRequest
import kotlinx.coroutines.Dispatchers

class ForgotPasswordRepoImpl constructor(
    private val apiService: ApiService,
) : BaseRepo(apiService), ForgotPasswordRepo {

    override suspend fun executeForgotPasswordRequest(request: ForgotPasswordRequest)
            : ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO){
            apiService.forgotPassword(request)
        }
    }
}