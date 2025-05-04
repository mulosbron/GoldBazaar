package com.mulosbron.goldbazaar.repository

import com.mulosbron.goldbazaar.model.request.AuthRequest
import com.mulosbron.goldbazaar.model.request.ForgotPasswordRequest
import com.mulosbron.goldbazaar.model.request.ResetPasswordRequest
import com.mulosbron.goldbazaar.model.response.AuthResponse
import com.mulosbron.goldbazaar.model.response.ForgotPasswordResponse
import com.mulosbron.goldbazaar.model.response.ResetPasswordResponse
import com.mulosbron.goldbazaar.repository.interfaces.IAuthRepository
import com.mulosbron.goldbazaar.service.api.UserAPI
import com.mulosbron.goldbazaar.service.network.NetworkResult
import com.mulosbron.goldbazaar.util.SharedPrefsManager
import com.mulosbron.goldbazaar.util.ext.safeApiCall

class AuthRepository(
    private val userAPI: UserAPI,
    private val sharedPrefsManager: SharedPrefsManager
) : IAuthRepository {

    override suspend fun authenticateUser(authRequest: AuthRequest): NetworkResult<AuthResponse> {
        return safeApiCall {
            val response = userAPI.loginUserSuspend(authRequest)
            // Başarılı giriş sonrası token saklama
            if (response.isSuccessful() && response.token != null) {
                sharedPrefsManager.saveAuthToken(response.token)
                sharedPrefsManager.saveUsername(response.username ?: "")
                // RememberMe özelliği var, ama şu anda SharedPrefsManager'da karşılığı yok
                // İlgili işlevselliği SharedPrefsManager'a ekleyin veya burayı kaldırın
            }
            response
        }
    }

    override suspend fun registerNewUser(authRequest: AuthRequest): NetworkResult<AuthResponse> {
        return safeApiCall {
            val response = userAPI.registerUserSuspend(authRequest)
            // Başarılı kayıt sonrası token saklama
            if (response.isSuccessful() && response.token != null) {
                sharedPrefsManager.saveAuthToken(response.token)
                sharedPrefsManager.saveUsername(response.username ?: "")
            }
            response
        }
    }

    override suspend fun requestPasswordReset(email: String): NetworkResult<ForgotPasswordResponse> {
        return safeApiCall {
            val normalizedEmail = email.trim().lowercase()
            userAPI.forgotPasswordSuspend(ForgotPasswordRequest(normalizedEmail))
        }
    }

    override suspend fun confirmPasswordReset(
        token: String,
        newPassword: String
    ): NetworkResult<ResetPasswordResponse> {
        return safeApiCall {
            userAPI.resetPasswordSuspend(ResetPasswordRequest(token, newPassword))
        }
    }

    override suspend fun logoutCurrentUser(): Boolean {
        // API'ye çıkış bildirimi (isteğe bağlı)
        // Yerel oturum bilgilerini temizle
        sharedPrefsManager.clearUserData()
        return true
    }

    override fun isUserAuthenticated(): Boolean {
        return sharedPrefsManager.isUserLoggedIn()
    }
}