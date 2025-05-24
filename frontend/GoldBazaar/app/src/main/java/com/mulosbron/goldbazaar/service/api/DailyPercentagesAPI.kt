package com.mulosbron.goldbazaar.service.api

import com.mulosbron.goldbazaar.model.entity.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.remote.ApiResponse
import com.mulosbron.goldbazaar.model.remote.DailyPctPayload
import retrofit2.http.GET

interface DailyPercentagesAPI {
    @GET("api/gold-daily-percentages/latest")
    suspend fun getLatestDailyPercentages(
    ): ApiResponse<DailyPctPayload>
//    suspend fun getLatestDailyPercentagesSuspend(): Map<String, DailyGoldPercentage>

}