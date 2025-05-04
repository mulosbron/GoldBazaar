package com.mulosbron.goldbazaar.service.api

import com.mulosbron.goldbazaar.model.entity.DailyGoldPercentage
import retrofit2.http.GET

interface DailyPercentagesAPI {
    @GET("api/daily-percentages/latest")
    suspend fun getLatestDailyPercentagesSuspend(): Map<String, DailyGoldPercentage>
}