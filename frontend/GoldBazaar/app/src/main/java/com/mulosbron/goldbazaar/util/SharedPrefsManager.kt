package com.mulosbron.goldbazaar.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mulosbron.goldbazaar.util.Constants.Prefs.KEY_AUTH_TOKEN
import com.mulosbron.goldbazaar.util.Constants.Prefs.KEY_REFRESH_TOKEN
import com.mulosbron.goldbazaar.util.Constants.Prefs.KEY_TOKEN_EXPIRY
import com.mulosbron.goldbazaar.util.Constants.Prefs.KEY_USERNAME
import com.mulosbron.goldbazaar.util.Constants.Prefs.PREF_NAME

class SharedPrefsManager(context: Context) {
    private val standardPrefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val securePrefs: SharedPreferences

    companion object {
        // Önbellek zaman damgaları için anahtarlar
        private const val KEY_PRICES_LAST_FETCH = "prices_last_fetch"
        private const val KEY_PERCENTAGES_LAST_FETCH = "percentages_last_fetch"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_REFRESH_TOKEN_EXPIRY = "refresh_token_expiry"
        private const val KEY_TOKEN_REFRESH_COUNT = "token_refresh_count"

        // Hassas veri anahtarları
        private val SECURE_KEYS = setOf(
            KEY_AUTH_TOKEN,
            KEY_REFRESH_TOKEN,
            KEY_TOKEN_EXPIRY,
            KEY_REFRESH_TOKEN_EXPIRY
        )

        // Varsayılan token süresi: 1 gün
        private const val DEFAULT_TOKEN_DURATION = 86400000L // 24 saat

        // Encrypted SharedPreferences dosya adı
        private const val ENCRYPTED_PREF_NAME = "SecureAppPreferences"
    }

    init {

        // Encrypted SharedPreferences başlatma
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        securePrefs = EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isUserLoggedIn(): Boolean {
        return getAuthToken() != null && !isTokenExpired()
    }

    fun saveAuthToken(token: String) {
        securePrefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + DEFAULT_TOKEN_DURATION)
            .apply()
    }

    fun getAuthToken(): String? {
        return securePrefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun isTokenExpired(): Boolean {
        val expiryTime = getTokenExpiryTime()
        return System.currentTimeMillis() > expiryTime
    }

    fun getTokenExpiryTime(): Long {
        return securePrefs.getLong(KEY_TOKEN_EXPIRY, 0)
    }

    fun saveUsername(username: String) {
        standardPrefs.edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getUsername(): String {
        return standardPrefs.getString(KEY_USERNAME, "Kullanıcı") ?: "Kullanıcı"
    }

    fun clearUserData() {
        // Güvenli verileri temizle
        securePrefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_REFRESH_TOKEN_EXPIRY)
            .apply()

        // Standart verileri temizle
        standardPrefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_REMEMBER_ME)
            .remove(KEY_TOKEN_REFRESH_COUNT)
            .apply()
    }

    fun clearTokens() {
        securePrefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .remove(KEY_REFRESH_TOKEN_EXPIRY)
            .apply()

        standardPrefs.edit()
            .remove(KEY_TOKEN_REFRESH_COUNT)
            .apply()
    }

    fun saveDailyPricesLastFetchTime(timestamp: Long) {
        standardPrefs.edit()
            .putLong(KEY_PRICES_LAST_FETCH, timestamp)
            .apply()
    }

    fun getDailyPricesLastFetchTime(): Long {
        return standardPrefs.getLong(KEY_PRICES_LAST_FETCH, 0)
    }

    fun saveDailyPercentagesLastFetchTime(timestamp: Long) {
        standardPrefs.edit()
            .putLong(KEY_PERCENTAGES_LAST_FETCH, timestamp)
            .apply()
    }

    fun getDailyPercentagesLastFetchTime(): Long {
        return standardPrefs.getLong(KEY_PERCENTAGES_LAST_FETCH, 0)
    }

    fun clearMarketDataTimestamps() {
        standardPrefs.edit()
            .remove(KEY_PRICES_LAST_FETCH)
            .remove(KEY_PERCENTAGES_LAST_FETCH)
            .apply()
    }
}