package com.mulosbron.goldbazaar.model.request

import com.mulosbron.goldbazaar.util.ValidationUtils

data class ResetPasswordRequest(
    val token: String?,
    val newPassword: String?,
    val confirmPassword: String? = null
) {
    fun isValid(validationUtils: ValidationUtils): Boolean {
        return token != null && validationUtils.validateToken(token).isValid &&
                newPassword != null && validationUtils.validatePasswordLength(
            newPassword,
            6
        ).isValid &&
                (confirmPassword == null || (validationUtils.validatePasswordsMatch(
                    newPassword,
                    confirmPassword
                ).isValid))
    }

    fun hasMinimumSecurityRequirements(validationUtils: ValidationUtils): Boolean {
        return newPassword != null && validationUtils.validatePasswordSecurity(newPassword).isValid
    }
}