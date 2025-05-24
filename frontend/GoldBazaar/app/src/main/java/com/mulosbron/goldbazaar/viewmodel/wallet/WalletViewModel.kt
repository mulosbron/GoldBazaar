package com.mulosbron.goldbazaar.viewmodel.wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mulosbron.goldbazaar.model.entity.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.entity.DailyGoldPrice
import com.mulosbron.goldbazaar.model.entity.Transaction
import com.mulosbron.goldbazaar.repository.WalletLocalRepository
import com.mulosbron.goldbazaar.repository.interfaces.IMarketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.util.Log

class WalletViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val TAG = "WalletVM"
    private val repository = WalletLocalRepository(application)
    private val marketRepository: IMarketRepository by inject()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val _uiState = MutableLiveData<WalletUiState>(WalletUiState.Idle)
    val uiState: LiveData<WalletUiState> = _uiState

    // LiveData for transactions
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    // LiveData for assets (gold types)
    private val _assets = MutableLiveData<List<String>>()
    val assets: LiveData<List<String>> get() = _assets

    // LiveData for average prices
    private val _averagePrices = MutableLiveData<Map<String, Double>>()
    val averagePrices: LiveData<Map<String, Double>> get() = _averagePrices

    // LiveData for profits
    private val _profits = MutableLiveData<Map<String, Double>>()
    val profits: LiveData<Map<String, Double>> get() = _profits

    // LiveData for selected asset transactions
    private val _selectedAssetTransactions = MutableLiveData<List<Transaction>>()
    val selectedAssetTransactions: LiveData<List<Transaction>> get() = _selectedAssetTransactions

    // Current selected asset
    private val _selectedAsset = MutableLiveData<String?>()
    val selectedAsset: LiveData<String?> get() = _selectedAsset

    // Market data
    private val _goldPrices = MutableLiveData<Map<String, DailyGoldPrice>>()
    val goldPrices: LiveData<Map<String, DailyGoldPrice>> get() = _goldPrices

    private val _dailyPercentages = MutableLiveData<Map<String, DailyGoldPercentage>>()
    val dailyPercentages: LiveData<Map<String, DailyGoldPercentage>> get() = _dailyPercentages

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = WalletUiState.Loading
        viewModelScope.launch {
            try {
                // 1️⃣ LOCAL --------------------------------------------------
                val allTransactions = repository.getAllTransactions()
                val assetList       = repository.getUniqueAssets()
                Log.d(TAG, "TX count = ${allTransactions.size}  assets = $assetList")

                _transactions.value = allTransactions
                _assets.value       = assetList

                // 2️⃣ MARKET -------------------------------------------------
                val pricesResult       = marketRepository.getDailyPrices()
                val percentagesResult  = marketRepository.getDailyPercentages()

                Log.d(TAG, "pricesResult = $pricesResult")
                Log.d(TAG, "pctResult    = $percentagesResult")

                val marketPrices = if (pricesResult is com.mulosbron.goldbazaar.service.network.NetworkResult.Success)
                    pricesResult.data else emptyMap()
                val marketPct   = if (percentagesResult is com.mulosbron.goldbazaar.service.network.NetworkResult.Success)
                    percentagesResult.data else emptyMap()

                Log.d(TAG, "marketPrices.keys = ${marketPrices.keys}")

                _goldPrices.value       = marketPrices
                _dailyPercentages.value = marketPct

                // 3️⃣ CALC ---------------------------------------------------
                val avgPrices = mutableMapOf<String, Double>()
                val profitMap = mutableMapOf<String, Double>()

                assetList.forEach { asset ->
                    avgPrices[asset] = repository.calculateAverageBuyingPrice(asset)

                    val cur = marketPrices[asset]?.sellingPrice ?: 0.0
                    Log.d(TAG, "asset=$asset  curPrice=$cur")

                    profitMap[asset] = repository.calculateProfit(asset, cur)
                }

                Log.d(TAG, "avgPrices = $avgPrices")
                Log.d(TAG, "profitMap = $profitMap")

                _averagePrices.value = avgPrices
                _profits.value       = profitMap
                _uiState.value       = WalletUiState.Success
            } catch (e: Exception) {
                Log.e(TAG, "loadData error", e)
                _uiState.value = WalletUiState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }

    fun loadTransactionsForAsset(asset: String) {
        _uiState.value = WalletUiState.Loading
        viewModelScope.launch {
            try {
                _selectedAsset.value = asset
                val assetTransactions = repository.getTransactionsByAsset(asset)
                _selectedAssetTransactions.value = assetTransactions
                _uiState.value = WalletUiState.Success
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error(e.message ?: "İşlemler yüklenirken bir hata oluştu")
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        _uiState.value = WalletUiState.Loading
        viewModelScope.launch {
            try {
                repository.addTransaction(transaction)
                if (_selectedAsset.value == transaction.asset) {
                    val assetTransactions = repository.getTransactionsByAsset(transaction.asset)
                    _selectedAssetTransactions.value = assetTransactions
                }
                loadData()
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error(e.message ?: "İşlem eklenirken bir hata oluştu")
            }
        }
    }

    fun updateTransaction(transactionId: String, updatedTransaction: Transaction) {
        _uiState.value = WalletUiState.Loading
        viewModelScope.launch {
            try {
                repository.updateTransaction(transactionId, updatedTransaction)
                if (_selectedAsset.value == updatedTransaction.asset) {
                    val assetTransactions = repository.getTransactionsByAsset(updatedTransaction.asset)
                    _selectedAssetTransactions.value = assetTransactions
                }
                loadData()
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error(e.message ?: "İşlem güncellenirken bir hata oluştu")
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        _uiState.value = WalletUiState.Loading
        viewModelScope.launch {
            try {
                val transactionToDelete = repository.getAllTransactions().find { it.id == transactionId }
                val assetOfTransaction = transactionToDelete?.asset

                repository.deleteTransaction(transactionId)

                if (_selectedAsset.value == assetOfTransaction) {
                    val assetTransactions = repository.getTransactionsByAsset(assetOfTransaction!!)
                    _selectedAssetTransactions.value = assetTransactions
                }

                loadData()
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error(e.message ?: "İşlem silinirken bir hata oluştu")
            }
        }
    }

    fun deleteAsset(asset: String) {
        _uiState.value = WalletUiState.Loading
        viewModelScope.launch {
            try {
                repository.deleteAssetTransactions(asset)
                if (_selectedAsset.value == asset) {
                    _selectedAssetTransactions.value = emptyList()
                    _selectedAsset.value = null
                }
                loadData()
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error(e.message ?: "Varlık silinirken bir hata oluştu")
            }
        }
    }

    // Get available assets for adding new transactions
    fun getAvailableAssets(): List<String> {
        return _goldPrices.value?.keys?.toList() ?: emptyList()
    }

    // fiyatı getirirken de log ekle
    fun getCurrentPrice(asset: String, isBuying: Boolean): Double {
        val key       = asset.replace("[\\u00A0\\s]+".toRegex(), " ").trim()
        val priceObj  = _goldPrices.value?.get(key)
        val raw       = if (isBuying) priceObj?.buyingPrice else priceObj?.sellingPrice
        Log.d(TAG, "getCurrentPrice key='$key'  value=$raw")
        return raw ?: 0.0
    }



//    fun getCurrentPrice(asset: String, isBuying: Boolean): Double {
//        return try {
//            val prices = _goldPrices.value?.get(asset)
//            if (isBuying) {
//                prices?.buyingPrice?.toDouble() ?: 0.0
//            } else {
//                prices?.sellingPrice?.toDouble() ?: 0.0
//            }
//        } catch (e: Exception) {
//            0.0
//        }
//    }

    fun calculateTotalPortfolioValue(): Double {
        try {
            var total = 0.0
            _assets.value?.forEach { asset ->
                val tx = repository.getTransactionsByAsset(asset)
                val net = tx.sumOf {
                    if (it.transactionType == "buy") it.amount
                    else -it.amount
                }
                val cur = _goldPrices.value?.get(asset)?.sellingPrice ?: 0.0
                Log.d(TAG, "PORF asset=$asset  netAmount=$net  cur=$cur")
                total += net * cur
            }
            Log.d(TAG, "PORTF TOTAL = $total")
            return total
        } catch (e: Exception) {
            Log.e(TAG, "calcTotal error", e)
            return 0.0
        }
    }
}