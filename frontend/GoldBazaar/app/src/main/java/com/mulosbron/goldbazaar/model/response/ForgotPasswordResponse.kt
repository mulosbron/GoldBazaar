package com.mulosbron.goldbazaar.model.response

import com.mulosbron.goldbazaar.util.ext.capitalize

data class ForgotPasswordResponse(
    override val message: String,
    val emailSent: Boolean = false,
    val expiryInSeconds: Int = 0,
    override val success: Boolean = false
) : BaseApiResponse {
    override fun isSuccessful(): Boolean {
        return success && emailSent
    }

    override fun getFormattedMessage(): String {
        return message.replace("_", " ").capitalize()
    }
}