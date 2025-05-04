package com.mulosbron.goldbazaar.viewmodel.auth

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class LoginSuccess(
        val token: String,
        val username: String,
        val expiryTime: String
    ) : AuthUiState()

    data class RegisterSuccess(val message: String) : AuthUiState()
    data class ForgotPasswordSuccess(val message: String) : AuthUiState()
    data class ResetPasswordSuccess(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}