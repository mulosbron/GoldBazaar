package com.mulosbron.goldbazaar.util

import android.os.Build

object Constants {

    object Device {
        val INFO = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}), " +
                "Model: ${Build.MANUFACTURER} ${Build.MODEL}"
    }

    object Prefs {
        const val PREF_NAME = "AppPreferences"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_EXPIRY = "token_expiry"
        const val KEY_USERNAME = "username"
    }

    const val IS_DEBUG = true  // Geliştirme sırasında true, release için false yapılmalı
}