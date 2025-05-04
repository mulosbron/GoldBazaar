package com.mulosbron.goldbazaar.model.response

interface BaseApiResponse {
    val message: String
    val success: Boolean

    fun isSuccessful(): Boolean

    fun getFormattedMessage(): String
}