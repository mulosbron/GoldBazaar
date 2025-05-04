package com.mulosbron.goldbazaar.util.ext

import com.mulosbron.goldbazaar.service.network.ApiErrorType
import com.mulosbron.goldbazaar.service.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            NetworkResult.success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> {
                    if (throwable is SocketTimeoutException) {
                        NetworkResult.standardError(ApiErrorType.TIMEOUT)
                    } else {
                        NetworkResult.standardError(ApiErrorType.NETWORK_ERROR)
                    }
                }

                is HttpException -> {
                    val errorCode = throwable.code()
                    // val errorBody = throwable.response()?.errorBody()?.string()

                    // HTTP hata koduna göre hata tipini belirle
                    val errorType = when (errorCode) {
                        401 -> ApiErrorType.UNAUTHORIZED
                        403 -> ApiErrorType.FORBIDDEN
                        404 -> ApiErrorType.NOT_FOUND
                        in 500..599 -> ApiErrorType.SERVER_ERROR
                        400 -> {
                            // Özel iş mantığı hatalarını belirle (örneğin, backend'den gelen özel kodlara göre)
                            // Burada backend'den gelen hata kodunu parse edebilirsiniz
                            // Örneğin: if (errorBody?.contains("insufficient_funds") == true) ApiErrorType.INSUFFICIENT_FUNDS else ApiErrorType.INVALID_REQUEST
                            ApiErrorType.INVALID_REQUEST
                        }

                        429 -> ApiErrorType.RATE_LIMIT_EXCEEDED
                        else -> ApiErrorType.UNKNOWN
                    }

                    NetworkResult.standardError(errorType, errorCode)
                }

                else -> {
                    NetworkResult.error(
                        ApiErrorType.UNKNOWN,
                        "Beklenmeyen bir hata oluştu: ${throwable.message}",
                        null
                    )
                }
            }
        }
    }
}