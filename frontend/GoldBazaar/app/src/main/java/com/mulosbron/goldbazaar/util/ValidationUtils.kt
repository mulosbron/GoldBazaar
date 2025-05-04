package com.mulosbron.goldbazaar.util

class ValidationUtils {
    companion object {
        private const val EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"

        private const val HAS_UPPERCASE = ".*[A-Z].*"
        private const val HAS_LOWERCASE = ".*[a-z].*"
        private const val HAS_NUMBER = ".*[0-9].*"
        private const val HAS_SPECIAL_CHAR = ".*[^A-Za-z0-9].*"
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateEmail(email: String): ValidationResult {
        if (email.isEmpty()) {
            return ValidationResult(false, "E-posta adresi boş olamaz.")
        }

        if (!email.matches(EMAIL_REGEX.toRegex())) {
            return ValidationResult(false, "Geçerli bir e-posta adresi giriniz.")
        }

        return ValidationResult(true)
    }

    fun validatePasswordLength(password: String, minLength: Int = 8): ValidationResult {
        if (password.isEmpty()) {
            return ValidationResult(false, "Şifre boş olamaz.")
        }

        if (password.length < minLength) {
            return ValidationResult(false, "Şifre en az $minLength karakter uzunluğunda olmalıdır.")
        }

        return ValidationResult(true)
    }

    fun validatePasswordSecurity(password: String): ValidationResult {
        val missingCriteria = mutableListOf<String>()

        if (!password.matches(HAS_UPPERCASE.toRegex())) {
            missingCriteria.add("en az bir büyük harf")
        }

        if (!password.matches(HAS_LOWERCASE.toRegex())) {
            missingCriteria.add("en az bir küçük harf")
        }

        if (!password.matches(HAS_NUMBER.toRegex())) {
            missingCriteria.add("en az bir rakam")
        }

        if (!password.matches(HAS_SPECIAL_CHAR.toRegex())) {
            missingCriteria.add("en az bir özel karakter")
        }

        if (missingCriteria.isNotEmpty()) {
            val errorMessage = buildString {
                append("Şifreniz ")
                append(missingCriteria.joinToString(", "))
                append(" içermelidir.")
            }
            return ValidationResult(false, errorMessage)
        }

        return ValidationResult(true)
    }

    fun validateToken(token: String): ValidationResult {
        if (token.isEmpty()) {
            return ValidationResult(false, "Geçersiz token. Lütfen tekrar giriş yapın.")
        }

        // JWT formatı kontrol edilebilir (isteğe bağlı)
        val jwtRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$"
        if (token.matches(jwtRegex.toRegex())) {
            return ValidationResult(true)
        }

        // JWT olmasa bile, token dolu ise kabul et
        return ValidationResult(true)
    }

    fun validatePasswordsMatch(password: String, confirmPassword: String): ValidationResult {
        if (password != confirmPassword) {
            return ValidationResult(false, "Şifreler eşleşmiyor. Lütfen aynı şifreyi girin.")
        }

        return ValidationResult(true)
    }
}