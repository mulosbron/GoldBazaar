package com.mulosbron.goldbazaar.model

data class DailyGoldPrice(
    val name: String,
    val buyingPrice: Int,
    val sellingPrice: Int
)

data class DailyGoldPercentage(
    val name: String,
    val buyingPrice: Double,
    val sellingPrice: Double
)
