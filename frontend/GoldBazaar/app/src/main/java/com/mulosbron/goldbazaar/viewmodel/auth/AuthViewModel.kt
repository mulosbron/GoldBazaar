package com.mulosbron.goldbazaar.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mulosbron.goldbazaar.model.request.AuthRequest
import com.mulosbron.goldbazaar.model.request.ForgotPasswordRequest
import com.mulosbron.goldbazaar.model.request.ResetPasswordRequest
import com.mulosbron.goldbazaar.repository.interfaces.IAuthRepository
import com.mulosbron.goldbazaar.service.network.NetworkResult
import com.mulosbron.goldbazaar.util.StringProvider
import com.mulosbron.goldbazaar.util.ValidationUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: IAuthRepository,
    private val validationUtils: ValidationUtils,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _uiState = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val uiState: LiveData<AuthUiState> = _uiState

    fun loginUser(email: String, password: String, rememberMe: Boolean = false) {
        // Model sınıfını kullanarak doğrulama yap
        val authRequest = AuthRequest(email, password, rememberMe)

        if (!authRequest.isValidForLogin(validationUtils)) {
            _uiState.value = AuthUiState.Error(stringProvider.getInvalidEmailOrPasswordString())
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            // Normalize edilmiş e-posta kullan
            val normalizedRequest = AuthRequest(
                authRequest.getNormalizedEmail(),
                password,
                rememberMe
            )

            // async/await kullanarak isteği gerçekleştir
            val result = async { authRepository.authenticateUser(normalizedRequest) }.await()

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response.isSuccessful()) {
                        val username =
                            response.username ?: response.message.split(" ").lastOrNull() ?: ""
                        _uiState.value = AuthUiState.LoginSuccess(
                            token = response.token ?: "",
                            username = username,
                            expiryTime = response.getExpiryFormatted(stringProvider)
                        )
                    } else {
                        _uiState.value = AuthUiState.Error(response.getFormattedMessage())
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.errorMessage)
                }

                is NetworkResult.Loading -> {
                    _uiState.value = AuthUiState.Loading
                }
            }
        }
    }

    fun registerUser(email: String, password: String) {
        // Model sınıfını kullanarak doğrulama yap
        val authRequest = AuthRequest(email, password)

        if (!authRequest.isValidForRegistration(validationUtils)) {
            _uiState.value = AuthUiState.Error(stringProvider.getInvalidEmailOrPasswordString())
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            // Normalize edilmiş e-posta kullan
            val normalizedRequest = AuthRequest(
                authRequest.getNormalizedEmail(),
                password
            )

            // async/await kullanarak isteği gerçekleştir
            val result = async { authRepository.registerNewUser(normalizedRequest) }.await()

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    _uiState.value = AuthUiState.RegisterSuccess(response.getFormattedMessage())
                }

                is NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.errorMessage)
                }

                is NetworkResult.Loading -> {
                    _uiState.value = AuthUiState.Loading
                }
            }
        }
    }

    fun forgotPassword(email: String) {
        // Model sınıfını kullanarak doğrulama yap
        val forgotPasswordRequest = ForgotPasswordRequest(email)

        if (!forgotPasswordRequest.isValid(validationUtils)) {
            _uiState.value = AuthUiState.Error(stringProvider.getInvalidEmailString())
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            // Normalize edilmiş e-posta kullan
            val normalizedEmail = forgotPasswordRequest.getNormalizedEmail()

            // async/await kullanarak isteği gerçekleştir
            val result = async { authRepository.requestPasswordReset(normalizedEmail) }.await()

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response.isSuccessful()) {  // Artık isEmailSent() yerine isSuccessful() kullanılıyor
                        _uiState.value =
                            AuthUiState.ForgotPasswordSuccess(response.getFormattedMessage())
                    } else {
                        _uiState.value =
                            AuthUiState.Error(stringProvider.getEmailSendFailedString(response.message))
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.errorMessage)
                }

                is NetworkResult.Loading -> {
                    _uiState.value = AuthUiState.Loading
                }
            }
        }
    }

    fun resetPassword(token: String, newPassword: String, confirmPassword: String? = null) {
        // Model sınıfını kullanarak doğrulama yap
        val resetRequest = ResetPasswordRequest(token, newPassword, confirmPassword)

        if (!resetRequest.isValid(validationUtils)) {
            _uiState.value = AuthUiState.Error(stringProvider.getPasswordLengthErrorString())
            return
        }

        // Ek güvenlik kontrolleri
        if (!resetRequest.hasMinimumSecurityRequirements(validationUtils)) {
            _uiState.value =
                AuthUiState.Error(stringProvider.getPasswordSecurityRequirementsString())
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            // async/await kullanarak isteği gerçekleştir
            val result = async { authRepository.confirmPasswordReset(token, newPassword) }.await()

            when (result) {
                is NetworkResult.Success -> {
                    val response = result.data
                    if (response.isSuccessful()) {
                        _uiState.value =
                            AuthUiState.ResetPasswordSuccess(response.getFormattedMessage())
                    } else {
                        _uiState.value = AuthUiState.Error(response.message)
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.errorMessage)
                }

                is NetworkResult.Loading -> {
                    _uiState.value = AuthUiState.Loading
                }
            }
        }
    }
}