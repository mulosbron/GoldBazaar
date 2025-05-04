package com.mulosbron.goldbazaar

import android.app.Application
import com.mulosbron.goldbazaar.di.AppModule
import com.mulosbron.goldbazaar.service.network.RetrofitClient
import com.mulosbron.goldbazaar.util.SharedPrefsManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GoldBazaarApplication : Application() {

    private val sharedPrefsManager: SharedPrefsManager by inject()

    override fun onCreate() {
        super.onCreate()

        // Koin bağımlılık enjeksiyon yapılandırması
        startKoin {
            androidContext(this@GoldBazaarApplication)
            modules(AppModule.modules)
        }

        // RetrofitClient'ı başlat - SharedPrefsManager Koin tarafından sağlanır
        RetrofitClient.initialize(
            context = this,
            prefsManager = sharedPrefsManager
        )
    }
}
// https://newsapi.org/v2/everything?q=alt%C4%B1n&language=tr&sortBy=publishedAt&apiKey=55b09ac83042479a8ba30e53fae85559