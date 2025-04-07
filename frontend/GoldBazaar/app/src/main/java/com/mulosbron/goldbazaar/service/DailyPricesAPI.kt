package com.mulosbron.goldbazaar.service

import com.mulosbron.goldbazaar.model.DailyGoldPrice
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface DailyPricesAPI {
    @GET("api/gold-prices/latest")
    fun getLatestGoldPrices(): Observable<Map<String, DailyGoldPrice>>

    @GET("api/gold-prices/latest/{product}")
    fun getGoldPrice(@Path("product") productName: String): Call<DailyGoldPrice>
}


