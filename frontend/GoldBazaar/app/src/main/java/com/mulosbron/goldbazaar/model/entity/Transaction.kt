package com.mulosbron.goldbazaar.model.entity

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(), // Unique identifier for each transaction
    val asset: String, // e.g., "22 Ayar Altın"
    val transactionType: String, // "buy" or "sell"
    val amount: Double,
    val date: String,
    val price: Double
)