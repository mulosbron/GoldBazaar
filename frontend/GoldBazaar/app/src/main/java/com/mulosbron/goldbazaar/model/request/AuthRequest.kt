package com.mulosbron.goldbazaar.model.request

import com.mulosbron.goldbazaar.util.ValidationUtils

data class AuthRequest(
    val email: String?,
    val password: String?,
    val rememberMe: Boolean = false
) {
    fun isValidForLogin(validationUtils: ValidationUtils): Boolean {
        return email != null && validationUtils.validateEmail(email).isValid && !password.isNullOrEmpty()
    }

    fun isValidForRegistration(validationUtils: ValidationUtils): Boolean {
        return email != null && validationUtils.validateEmail(email).isValid &&
                password != null && validationUtils.validatePasswordLength(password, 6).isValid
    }

    fun getNormalizedEmail(): String {
        return email?.lowercase()?.trim() ?: ""
    }
}