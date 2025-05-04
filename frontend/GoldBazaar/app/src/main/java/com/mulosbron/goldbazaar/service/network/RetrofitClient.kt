package com.mulosbron.goldbazaar.service.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.mulosbron.goldbazaar.util.AuthConstants
import com.mulosbron.goldbazaar.util.Constants
import com.mulosbron.goldbazaar.util.NetworkConstants
import com.mulosbron.goldbazaar.util.SharedPrefsManager
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TAG = "RetrofitClient"
    private var appContext: Context? = null
    private var sharedPrefsManager: SharedPrefsManager? = null

    fun initialize(context: Context, prefsManager: SharedPrefsManager) {
        appContext = context.applicationContext // Uygulama context'ini kullan
        sharedPrefsManager = prefsManager

        if (Constants.IS_DEBUG) {
            Log.d(TAG, "RetrofitClient başarıyla initialize edildi.")
        }
    }

    private fun isProperlyInitialized(): Boolean {
        val initialized = appContext != null && sharedPrefsManager != null

        if (!initialized && Constants.IS_DEBUG) {
            Log.w(TAG, "RetrofitClient henüz initialize edilmemiş!")
        }

        return initialized
    }

    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val prefs = sharedPrefsManager ?: return@Interceptor proceedWithBasicHeaders(
                chain,
                originalRequest
            )
            val token = prefs.getAuthToken()

            // Token olmadan istekleri gönder
            token ?: return@Interceptor proceedWithBasicHeaders(chain, originalRequest)

            // Token'ın geçerlilik süresini kontrol et
            if (prefs.isTokenExpired() ||
                System.currentTimeMillis() > (prefs.getTokenExpiryTime() - AuthConstants.TOKEN_REFRESH_THRESHOLD_MS)
            ) {
                refreshToken(prefs)
            }

            // Güncel token ile isteğe devam et
            val updatedToken = prefs.getAuthToken()
                ?: return@Interceptor proceedWithBasicHeaders(chain, originalRequest)

            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer $updatedToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "GoldBazaar-Android/${Constants.Device.INFO}")
                .method(originalRequest.method, originalRequest.body)
                .build()

            // İsteği gönder
            val response = chain.proceed(requestWithAuth)

            // 401 hatası durumunda token yenileme işlemi
            if (response.code == NetworkConstants.ErrorCodes.UNAUTHORIZED) {
                response.close()

                // Token yenileme dene
                val tokenRefreshed = refreshToken(prefs)

                // Token yenilendiyse, yeni token ile isteği tekrarla
                if (tokenRefreshed) {
                    val newToken = prefs.getAuthToken()
                    if (newToken != null) {
                        val newRequestWithAuth = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("User-Agent", "GoldBazaar-Android/${Constants.Device.INFO}")
                            .method(originalRequest.method, originalRequest.body)
                            .build()

                        // Yeni token ile isteği gönder
                        return@Interceptor chain.proceed(newRequestWithAuth)
                    }
                }

                // Token yenilenemedi, aynı hata yanıtını döndür
                return@Interceptor response
            }

            response
        }
    }

    private fun proceedWithBasicHeaders(chain: Interceptor.Chain, request: Request): Response {
        val requestWithBasicHeaders = request.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", "GoldBazaar-Android/${Constants.Device.INFO}")
            .method(request.method, request.body)
            .build()
        return chain.proceed(requestWithBasicHeaders)
    }

    private fun refreshToken(sharedPrefsManager: SharedPrefsManager): Boolean {
        if (Constants.IS_DEBUG) Log.d(TAG, "Token expired, user needs to re-login")

        // Tokenları temizle
        sharedPrefsManager.clearTokens()

        // EventBus veya diğer bir mesajlaşma mekanizması ile kullanıcının
        // giriş ekranına yönlendirilmesi gerektiğini bildir
        // Örnek: EventBus.getDefault().post(TokenExpiredEvent())

        return false // Token yenileme her zaman başarısız - kullanıcının tekrar giriş yapması gerekli
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (Constants.IS_DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }

    private fun createNetworkLogInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            if (Constants.IS_DEBUG) {
                val startTime = System.currentTimeMillis()
                Log.d(TAG, "--> ${request.method} ${request.url}")
                request.headers.forEach { (name, value) ->
                    Log.d(TAG, "$name: $value")
                }

                val response = chain.proceed(request)
                val duration = System.currentTimeMillis() - startTime

                Log.d(TAG, "<-- ${response.code} ${response.message} (${duration}ms)")
                response.headers.forEach { (name, value) ->
                    Log.d(TAG, "$name: $value")
                }

                return@Interceptor response
            }

            chain.proceed(request)
        }
    }

    private fun createCacheInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val context = appContext ?: return@Interceptor chain.proceed(request)

            // Assignment'ı if dışına çıkararak optimize edildi
            val requestBuilder = request.newBuilder()

            // Çevrimiçi/çevrimdışı durumuna göre uygun cache header'ını ekle
            if (isNetworkAvailable(context)) {
                requestBuilder.header(
                    "Cache-Control",
                    "public, max-age=${NetworkConstants.MAX_AGE_SECONDS}"
                )
            } else {
                requestBuilder.header(
                    "Cache-Control",
                    "public, only-if-cached, max-stale=${NetworkConstants.MAX_STALE_SECONDS}"
                )
            }

            chain.proceed(requestBuilder.build())
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Android'in modern API'lerini kullan (SDK 23+ için)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun createOkHttpClient(): OkHttpClient {
        val context = appContext
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(createNetworkLogInterceptor())
            .connectTimeout(NetworkConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        // Kimlik doğrulama interceptor'ını ekle
        if (sharedPrefsManager != null) {
            builder.addInterceptor(createAuthInterceptor())
        }

        // Cache ve network interceptor'ı ekle
        if (context != null) {
            // Cache dizinini oluştur
            val cacheDir = File(context.cacheDir, "http-cache")
            val cacheSize = NetworkConstants.CACHE_SIZE_MB * 1024 * 1024 // MB -> Byte
            val cache = Cache(cacheDir, cacheSize)

            builder.cache(cache)
            builder.addNetworkInterceptor(createCacheInterceptor())
        }

        return builder.build()
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        // İnitialize edilmişse, gelişmiş yapılandırmayı kullan
        if (isProperlyInitialized()) {
            return createRetrofit().create(serviceClass)
        }

        // İnitialize edilmemişse, basit yapılandırmayla ilerle
        Log.w(
            TAG, "RetrofitClient initialize edilmemiş! Basit yapılandırma kullanılıyor. " +
                    "initialize(context, prefsManager) metodunu çağırdığınızdan emin olun."
        )

        val simpleOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(NetworkConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(simpleOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(serviceClass)
    }
}