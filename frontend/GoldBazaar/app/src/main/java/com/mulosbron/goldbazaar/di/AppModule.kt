package com.mulosbron.goldbazaar.di

import com.mulosbron.goldbazaar.repository.AuthRepository
import com.mulosbron.goldbazaar.repository.MarketRepository
import com.mulosbron.goldbazaar.repository.NewsRepository
import com.mulosbron.goldbazaar.repository.interfaces.IAuthRepository
import com.mulosbron.goldbazaar.repository.interfaces.IMarketRepository
import com.mulosbron.goldbazaar.repository.interfaces.INewsRepository
import com.mulosbron.goldbazaar.service.network.RetrofitClient
import com.mulosbron.goldbazaar.util.SharedPrefsManager
import com.mulosbron.goldbazaar.util.StringProvider
import com.mulosbron.goldbazaar.util.ValidationUtils
import com.mulosbron.goldbazaar.viewmodel.auth.AuthViewModel
import com.mulosbron.goldbazaar.viewmodel.market.MarketViewModel
import com.mulosbron.goldbazaar.viewmodel.news.NewsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModule {
    private val networkModule: Module = module {
        single { RetrofitClient }
        single { get<RetrofitClient>().createService(com.mulosbron.goldbazaar.service.api.UserAPI::class.java) }
        single { get<RetrofitClient>().createService(com.mulosbron.goldbazaar.service.api.DailyPricesAPI::class.java) }
        single { get<RetrofitClient>().createService(com.mulosbron.goldbazaar.service.api.DailyPercentagesAPI::class.java) }
        single { get<RetrofitClient>().createService(com.mulosbron.goldbazaar.service.api.NewsAPI::class.java) }
    }

    private val utilityModule: Module = module {
        single { ValidationUtils() }
        single { SharedPrefsManager(androidContext()) }
        single { StringProvider(androidContext()) }
    }

    private val repositoryModule: Module = module {
        single<IAuthRepository> { AuthRepository(get(), get<SharedPrefsManager>()) }
        single<IMarketRepository> { MarketRepository(get(), get(), get<SharedPrefsManager>()) }
        single<INewsRepository> { NewsRepository(get()) }
    }

    private val viewModelModule: Module = module {
        viewModel { AuthViewModel(get(), get(), get()) }
        viewModel { MarketViewModel(get(), get()) }
        viewModel { NewsViewModel(get()) }
    }

    val modules = listOf(
        utilityModule,
        networkModule,
        repositoryModule,
        viewModelModule
    )
}