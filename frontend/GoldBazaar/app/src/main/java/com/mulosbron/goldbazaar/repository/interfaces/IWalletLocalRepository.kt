package com.mulosbron.goldbazaar.repository.interfaces

import com.mulosbron.goldbazaar.model.entity.Transaction

interface IWalletLocalRepository {
    fun getAllTransactions(): List<Transaction>
    fun getTransactionsByAsset(asset: String): List<Transaction>
    fun addTransaction(transaction: Transaction)
    fun updateTransaction(transactionId: String, updatedTransaction: Transaction)
    fun deleteTransaction(transactionId: String)
    fun deleteAssetTransactions(asset: String)
    fun getUniqueAssets(): List<String>
    fun calculateAverageBuyingPrice(asset: String): Double
    fun calculateProfit(asset: String, currentPrice: Double): Double
}
