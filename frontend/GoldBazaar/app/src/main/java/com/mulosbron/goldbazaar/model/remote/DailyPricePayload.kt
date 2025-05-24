package com.mulosbron.goldbazaar.model.remote

data class PricePayload(
    val date: String,
    val data: Map<String, GoldPriceDetail>
)

data class GoldPriceDetail(
    val buyingPrice: Double,
    val sellingPrice: Double
)