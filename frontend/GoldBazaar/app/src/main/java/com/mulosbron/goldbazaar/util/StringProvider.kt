package com.mulosbron.goldbazaar.util

import android.content.Context
import com.mulosbron.goldbazaar.R

class StringProvider(private val context: Context) {
    // Genel string kaynakları
    fun getNoDataString(): String = context.getString(R.string.no_data)
    fun getNotSpecifiedString(): String = context.getString(R.string.not_specified)
    fun getUnknownString(): String = context.getString(R.string.unknown)
    fun getNotCalculableString(): String = context.getString(R.string.not_calculable)

    // Hata mesajları
    fun getInvalidEmailOrPasswordString(): String =
        context.getString(R.string.invalid_email_or_password)

    fun getInvalidEmailString(): String = context.getString(R.string.invalid_email)
    fun getEmailSendFailedString(message: String): String =
        context.getString(R.string.email_send_failed, message)

    fun getPasswordLengthErrorString(): String = context.getString(R.string.password_length_error)
    fun getPasswordSecurityRequirementsString(): String =
        context.getString(R.string.password_security_requirements)

    // Zaman formatları
    fun getTimeFormatString(seconds: Int): String {
        if (seconds <= 0) return getNotSpecifiedString()

        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 && minutes > 0 -> context.getString(
                R.string.time_format_hours_minutes,
                hours,
                minutes
            )

            hours > 0 -> context.getString(R.string.time_format_hours, hours)
            minutes > 0 -> context.getString(R.string.time_format_minutes, minutes)
            else -> context.getString(R.string.time_format_seconds, seconds)
        }
    }
}