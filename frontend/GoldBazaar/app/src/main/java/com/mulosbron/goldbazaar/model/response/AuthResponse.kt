package com.mulosbron.goldbazaar.model.response

import com.mulosbron.goldbazaar.util.ext.capitalize

data class AuthResponse(
    override val message: String,
    val token: String? = null,
    val username: String? = null,
    val expiryInSeconds: Int = 0,
    override val success: Boolean = false
) : BaseApiResponse {
    override fun isSuccessful(): Boolean {
        return !token.isNullOrEmpty() && success
    }

    override fun getFormattedMessage(): String {
        return message.replace("_", " ").capitalize()
    }

    fun getExpiryFormatted(stringProvider: com.mulosbron.goldbazaar.util.StringProvider): String {
        return stringProvider.getTimeFormatString(expiryInSeconds)
    }
}