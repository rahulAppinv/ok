package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.ContactResponse
import com.app.okra.models.SettingRequest
import com.app.okra.models.SupportResponse
import kotlinx.coroutines.Dispatchers
import java.util.*

class SupportRequestRepoImpl constructor(
    private val apiService: ApiService,
) : BaseRepo(apiService),
        SupportRequestRepo {


    override suspend fun getSupportRequestList(params: WeakHashMap<String, Any>): ApiResult<ApiData<List<SupportResponse>>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getSupportRequestList(params)
        }
    }

    override suspend fun sendSupportRequest(params: WeakHashMap<String, Any>): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.sendSupportRequest(params)
        }
    }
}