package com.mulosbron.goldbazaar.model.entity

data class DailyGoldPrice(
    val name: String,
    val buyingPrice: Int? = null,
    val sellingPrice: Int? = null,
    val lastUpdated: String? = null
) {

    fun getCleanName(): String {
        return name.replace("Altın", "").trim()
    }
}