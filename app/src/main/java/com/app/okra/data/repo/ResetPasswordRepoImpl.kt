package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.ResetPasswordRequest
import kotlinx.coroutines.Dispatchers

class ResetPasswordRepoImpl constructor(
    private val apiService: ApiService,
) : BaseRepo(apiService),
    ResetPasswordRepo {

    override suspend fun executeResetPasswordRequest(request: ResetPasswordRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO){
            apiService.resetPassword(request)
        }
    }

    override suspend fun changePasswordRequest(request: ResetPasswordRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO){
            apiService.changePassword(request)
        }
    }
}