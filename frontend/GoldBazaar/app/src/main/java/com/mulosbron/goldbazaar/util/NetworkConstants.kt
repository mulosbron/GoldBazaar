package com.mulosbron.goldbazaar.util

object NetworkConstants {
    enum class Environment {
        DEVELOPMENT, STAGING, PRODUCTION
    }
    private val ACTIVE_ENVIRONMENT = Environment.DEVELOPMENT

    private const val DEV_BASE_URL = "http://10.0.2.2:5000/"
    private const val STAGING_BASE_URL = "https://staging-api.goldbazaar.com/"
    private const val PROD_BASE_URL = "https://api.goldbazaar.com/"

    val BASE_URL: String
        get() = when (ACTIVE_ENVIRONMENT) {
            Environment.DEVELOPMENT -> DEV_BASE_URL
            Environment.STAGING -> STAGING_BASE_URL
            Environment.PRODUCTION -> PROD_BASE_URL
        }

    const val CACHE_VALIDITY_DURATION = 300000L      // 5 dakika (önbellek geçerlilik süresi)
    const val NETWORK_TIMEOUT_SECONDS = 30L          // Ağ zaman aşımı
    const val CACHE_SIZE_MB = 10L                    // Önbellek boyutu (MB)
    const val MAX_STALE_SECONDS =
        60 * 60 * 24 * 7   // Çevrimdışıyken en fazla 1 haftalık önbellek kullanılır
    const val MAX_AGE_SECONDS =
        60 * 5               // Normal durumda en fazla 5 dakikalık önbellek kullanılır

    object ErrorCodes {
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val SERVER_ERROR = 500
    }
}