package com.mulosbron.goldbazaar.service

import com.mulosbron.goldbazaar.model.AuthRequest
import com.mulosbron.goldbazaar.model.AuthResponse
import com.mulosbron.goldbazaar.model.ForgotPasswordRequest
import com.mulosbron.goldbazaar.model.ForgotPasswordResponse
import com.mulosbron.goldbazaar.model.ResetPasswordRequest
import com.mulosbron.goldbazaar.model.ResetPasswordResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserAPI {
    @POST("api/users/login")
    fun loginUser(@Body authRequest: AuthRequest): Call<AuthResponse>

    @POST("api/users/register")
    fun registerUser(@Body authRequest: AuthRequest): Call<AuthResponse>

    @POST("api/users/forgot-password")
    fun forgotPassword(@Body forgotPasswordRequest: ForgotPasswordRequest): Call<ForgotPasswordResponse>

    @POST("api/users/reset-password")
    fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Call<ResetPasswordResponse>
}