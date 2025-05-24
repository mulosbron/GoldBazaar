package com.mulosbron.goldbazaar.model.remote

data class DailyPctPayload(
    val date: String,
    val percentageDifference: Map<String, PercentageChange>
)

data class PercentageChange(
    val buyingPrice: Double,
    val sellingPrice: Double
)