package com.mulosbron.goldbazaar.service.api

import com.mulosbron.goldbazaar.model.request.AuthRequest
import com.mulosbron.goldbazaar.model.request.ForgotPasswordRequest
import com.mulosbron.goldbazaar.model.request.ResetPasswordRequest
import com.mulosbron.goldbazaar.model.response.AuthResponse
import com.mulosbron.goldbazaar.model.response.ForgotPasswordResponse
import com.mulosbron.goldbazaar.model.response.ResetPasswordResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface UserAPI {
    @POST("api/users/login")
    suspend fun loginUserSuspend(@Body authRequest: AuthRequest): AuthResponse

    @POST("api/users/register")
    suspend fun registerUserSuspend(@Body authRequest: AuthRequest): AuthResponse

    @POST("api/users/forgot-password")
    suspend fun forgotPasswordSuspend(@Body forgotPasswordRequest: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("api/users/reset-password")
    suspend fun resetPasswordSuspend(@Body resetPasswordRequest: ResetPasswordRequest): ResetPasswordResponse
}