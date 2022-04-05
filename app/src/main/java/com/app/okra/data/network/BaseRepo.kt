package com.app.okra.data.network

import kotlinx.coroutines.Dispatchers

abstract class BaseRepo( apiService: ApiService) :ApiExecutor() {
    var apiServe: ApiService?=null
    init {
        this.apiServe = apiService
    }
    suspend fun onLogout(): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiServe?.logout()!!
        }
    }
}