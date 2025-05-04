package com.mulosbron.goldbazaar.service.api

import com.mulosbron.goldbazaar.model.entity.DailyGoldPrice
import retrofit2.http.GET

interface
DailyPricesAPI {
    @GET("api/gold-prices/latest")
    suspend fun getLatestGoldPricesSuspend(): Map<String, DailyGoldPrice>
}


