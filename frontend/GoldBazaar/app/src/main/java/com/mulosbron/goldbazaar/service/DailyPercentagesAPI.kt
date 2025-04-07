package com.mulosbron.goldbazaar.service

import com.mulosbron.goldbazaar.model.DailyGoldPercentage
import io.reactivex.Observable
import retrofit2.http.GET

interface DailyPercentagesAPI {
    @GET("api/daily-percentages/latest")
    fun getLatestDailyPercentages(): Observable<Map<String, DailyGoldPercentage>>
}