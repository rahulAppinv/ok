package com.app.okra.data.network

import com.app.okra.utils.MessageConstants
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

abstract class ApiExecutor {

    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        baseCall: suspend () -> Response<BaseResponse<T>>
    ): ApiResult<ApiData<T>> {
        return withContext(dispatcher) {
            try {
                val result = baseCall.invoke()
/*
           val baseResponse :BaseResponse<T> = Gson().fromJson(result.body.toString(), BaseResponse::class.java)
                   as BaseResponse<T>*/

                val apiResult = if (result.isSuccessful) {
                    ApiResult.Success(
                        ApiData(
                            data = result.body()?.data,
                            statusCode = result.body()?.statusCode,
                            message = result.body()?.message,
                            type = result.body()?.type,
                            error = result.body()?.error,
                            total = result.body()?.total,
                            pageNo = result.body()?.pageNo,
                            limit = result.body()?.limit,
                            totalPage = result.body()?.totalPage,
                            nextHit = result.body()?.nextHit)
                    )
                } else {

                    val baseResponse: BaseResponse<Any> = Gson().fromJson(result.errorBody()?.string(),
                        BaseResponse::class.java) as BaseResponse<Any>

                    ApiResult.GenericError(
                        message = baseResponse.message ?: "Unknown error",
                        errorCode = (baseResponse.statusCode ?: "0"),
                        type = (baseResponse.type ?: ""),
                        data = baseResponse.data
                    )
                }
                apiResult
            }
            catch (throwable: Throwable) {
                throwable.printStackTrace()
                println("Error: ${throwable.message}")
                when (throwable) {
                    is IOException -> {
                        ApiResult.NetworkError
                    }
                    is HttpException -> {
                        ApiResult.GenericError(throwable.code().toString(), convertErrorBody(throwable))
                    }
                    else -> {
                        ApiResult.GenericError(message = MessageConstants.Errors.an_error_occurred)
                    }
                }
            }
        }
    }

    suspend fun <T> safeApiCallWithoutBaseResponse(
        dispatcher: CoroutineDispatcher,
        baseCall: suspend () -> Response<T>
    ): ApiResult<T?> {
        return withContext(dispatcher) {
            try {
                val result = baseCall.invoke()

                val apiResult = if (result.isSuccessful) {
                    ApiResult.Success1(
                        result.body()
                    )
                }else {
                    val baseResponse: BaseResponse<Any> = Gson().fromJson(result.errorBody()?.string(),
                        BaseResponse::class.java) as BaseResponse<Any>

                    ApiResult.GenericError(
                        message = baseResponse.message ?: "Unknown error",
                        errorCode = (baseResponse.statusCode ?: "0"),
                        type = (baseResponse.type ?: ""),
                        data = baseResponse.data
                    )
                }
                apiResult
            }
            catch (throwable: Throwable) {
                throwable.printStackTrace()
                println("Error: ${throwable.message}")
                when (throwable) {
                    is IOException -> {
                        ApiResult.NetworkError
                    }
                    is HttpException -> {
                        ApiResult.GenericError(throwable.code().toString(), convertErrorBody(throwable))
                    }
                    else -> {
                        ApiResult.GenericError(message = MessageConstants.Errors.an_error_occurred)
                    }
                }
            }
        }
    }

    private fun convertErrorBody(throwable: HttpException): String {
        val errorStr = throwable.response()?.errorBody()?.string()
        if (errorStr != null) {
            val response = Gson().fromJson(errorStr, BaseResponse::class.java)
            return response.error ?:("Unknown error")
        }
        return "Unknown error"
    }



}