package com.mulosbron.goldbazaar.model.request

import com.mulosbron.goldbazaar.util.ValidationUtils

data class ForgotPasswordRequest(
    val email: String?
) {
    fun isValid(validationUtils: ValidationUtils): Boolean {
        return email != null && validationUtils.validateEmail(email).isValid
    }

    fun getNormalizedEmail(): String {
        return email?.lowercase()?.trim() ?: ""
    }
}
