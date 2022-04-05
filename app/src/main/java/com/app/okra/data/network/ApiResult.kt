package com.app.okra.data.network

sealed class ApiResult<out T> {
    data class Success<out T>(val value: ApiData<T>) : ApiResult<ApiData<T>>()
    data class Success1<out T>(val value: T) : ApiResult<T>()
    data class GenericError(val errorCode: String="",
                            val message: String,
                            val data: Any?=null,
                            val type :String = "") : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
}

