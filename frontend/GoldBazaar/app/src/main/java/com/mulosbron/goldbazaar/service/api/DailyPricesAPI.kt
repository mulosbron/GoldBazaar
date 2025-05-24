package com.mulosbron.goldbazaar.service.api

import com.mulosbron.goldbazaar.model.entity.DailyGoldPrice
import com.mulosbron.goldbazaar.model.remote.ApiResponse
import com.mulosbron.goldbazaar.model.remote.PricePayload
import retrofit2.http.GET

interface
DailyPricesAPI {
    @GET("api/gold-prices/latest")
//    suspend fun getLatestGoldPricesSuspend(): Map<String, DailyGoldPrice>
    suspend fun getLatestGoldPrices(): ApiResponse<PricePayload>
}


