package com.mulosbron.goldbazaar.service

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mulosbron.goldbazaar.model.DailyGoldPrice
import com.mulosbron.goldbazaar.model.DailyGoldPercentage
import com.mulosbron.goldbazaar.view.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ApiService(private val context: Fragment) {
    private val baseURL = "http://10.0.2.2:5000/"

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun fetchDailyPrices(
        compositeDisposable: CompositeDisposable,
        callback: (Map<String, DailyGoldPrice>) -> Unit
    ) {
        val goldPricesAPI = getRetrofit().create(DailyPricesAPI::class.java)
        compositeDisposable.add(
            goldPricesAPI.getLatestGoldPrices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    callback(response)
                }, { error ->
                    Toast.makeText(
                        context.requireContext(),
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                })
        )
    }

    fun fetchDailyPercentages(
        compositeDisposable: CompositeDisposable,
        callback: (Map<String, DailyGoldPercentage>) -> Unit
    ) {
        val dailyPercentagesAPI = getRetrofit().create(DailyPercentagesAPI::class.java)
        compositeDisposable.add(
            dailyPercentagesAPI
                .getLatestDailyPercentages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    callback(response)
                }, { error ->
                    Toast.makeText(
                        context.requireContext(),
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                })
        )
    }
}

