package com.mulosbron.goldbazaar.repository.interfaces

import com.mulosbron.goldbazaar.model.request.AuthRequest
import com.mulosbron.goldbazaar.model.response.AuthResponse
import com.mulosbron.goldbazaar.model.response.ForgotPasswordResponse
import com.mulosbron.goldbazaar.model.response.ResetPasswordResponse
import com.mulosbron.goldbazaar.service.network.NetworkResult

/**
 * Kimlik doğrulama ve kullanıcı işlemleri için repository arayüzü.
 * Kullanıcı girişi, kaydı ve şifre işlemleri için metotlar tanımlar.
 */
interface IAuthRepository {
    /**
     * Kullanıcı giriş işlemini gerçekleştirir.
     * @param authRequest Kullanıcı giriş bilgilerini içeren istek nesnesi
     * @return Giriş işlemi sonucu (token, kullanıcı bilgileri vb.)
     */
    suspend fun authenticateUser(authRequest: AuthRequest): NetworkResult<AuthResponse>

    /**
     * Yeni kullanıcı kaydı oluşturur.
     * @param authRequest Kullanıcı kayıt bilgilerini içeren istek nesnesi
     * @return Kayıt işlemi sonucu (token, kullanıcı bilgileri vb.)
     */
    suspend fun registerNewUser(authRequest: AuthRequest): NetworkResult<AuthResponse>

    /**
     * Şifre sıfırlama işlemi için e-posta gönderir.
     * @param email Kullanıcı e-posta adresi
     * @return Şifre sıfırlama e-postası gönderim sonucu
     */
    suspend fun requestPasswordReset(email: String): NetworkResult<ForgotPasswordResponse>

    /**
     * Şifre yenileme işlemini gerçekleştirir.
     * @param token Şifre yenileme token'ı
     * @param newPassword Yeni şifre
     * @return Şifre yenileme işlemi sonucu
     */
    suspend fun confirmPasswordReset(
        token: String,
        newPassword: String
    ): NetworkResult<ResetPasswordResponse>

    /**
     * Mevcut oturumu sonlandırır.
     * @return İşlem başarılı oldu mu
     */
    suspend fun logoutCurrentUser(): Boolean

    /**
     * Kullanıcının giriş yapmış durumda olup olmadığını kontrol eder.
     * @return Kullanıcı giriş yapmış mı
     */
    fun isUserAuthenticated(): Boolean
}