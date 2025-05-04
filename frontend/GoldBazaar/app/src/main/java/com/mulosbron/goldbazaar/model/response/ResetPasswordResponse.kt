package com.mulosbron.goldbazaar.model.response

import com.mulosbron.goldbazaar.util.ext.capitalize


data class ResetPasswordResponse(
    override val message: String,
    override val success: Boolean = false,
    val expiryInSeconds: Int = 0
) : BaseApiResponse {
    override fun isSuccessful(): Boolean {
        return success
    }

    override fun getFormattedMessage(): String {
        return message.replace("_", " ").capitalize()
    }
}