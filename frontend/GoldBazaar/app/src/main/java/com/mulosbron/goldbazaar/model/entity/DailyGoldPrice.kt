package com.mulosbron.goldbazaar.model.entity

data class DailyGoldPrice(
    val name: String,
    val buyingPrice: Double? = null,
    val sellingPrice: Double? = null,
    val lastUpdated: String? = null
) {

    fun getCleanName(): String {
        return name.replace("Altın", "").trim()
    }
}