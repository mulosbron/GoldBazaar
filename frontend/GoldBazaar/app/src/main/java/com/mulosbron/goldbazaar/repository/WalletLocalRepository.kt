package com.mulosbron.goldbazaar.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mulosbron.goldbazaar.model.entity.Transaction
import com.mulosbron.goldbazaar.repository.interfaces.IWalletLocalRepository

class WalletLocalRepository(context: Context) : IWalletLocalRepository {

    private val prefs = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val keyTransactions = "transactions_key"

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString(keyTransactions, json).apply()
    }

    private fun loadTransactions(): MutableList<Transaction> {
        val json = prefs.getString(keyTransactions, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    override fun getAllTransactions(): List<Transaction> {
        return loadTransactions()
    }

    override fun getTransactionsByAsset(asset: String): List<Transaction> {
        return loadTransactions().filter { it.asset == asset }
    }

    override fun addTransaction(transaction: Transaction) {
        val transactions = loadTransactions()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    override fun updateTransaction(transactionId: String, updatedTransaction: Transaction) {
        val transactions = loadTransactions()
        val index = transactions.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(transactions)
        }
    }

    override fun deleteTransaction(transactionId: String) {
        val transactions = loadTransactions().filterNot { it.id == transactionId }
        saveTransactions(transactions)
    }

    override fun deleteAssetTransactions(asset: String) {
        val transactions = loadTransactions().filterNot { it.asset == asset }
        saveTransactions(transactions)
    }

    override fun getUniqueAssets(): List<String> {
        return loadTransactions().map { it.asset }.distinct()
    }

    override fun calculateAverageBuyingPrice(asset: String): Double {
        val buys = getTransactionsByAsset(asset).filter { it.transactionType == "buy" }
        val totalAmount = buys.sumOf { it.amount }
        val totalCost = buys.sumOf { it.amount * it.price }
        return if (totalAmount > 0) totalCost / totalAmount else 0.0
    }

    override fun calculateProfit(asset: String, currentPrice: Double): Double {
        val assetTransactions = getTransactionsByAsset(asset)
        var netAmount = 0.0
        var totalInvestment = 0.0

        assetTransactions.forEach { transaction ->
            if (transaction.transactionType == "buy") {
                netAmount += transaction.amount
                totalInvestment += transaction.amount * transaction.price
            } else if (transaction.transactionType == "sell") {
                netAmount -= transaction.amount
                totalInvestment -= transaction.amount * transaction.price
            }
        }

        val currentValue = netAmount * currentPrice
        return currentValue - totalInvestment
    }
}