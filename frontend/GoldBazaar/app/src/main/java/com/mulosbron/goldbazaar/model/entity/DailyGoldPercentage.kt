package com.mulosbron.goldbazaar.model.entity

data class DailyGoldPercentage(
    val name: String,
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val lastUpdated: String? = null
)