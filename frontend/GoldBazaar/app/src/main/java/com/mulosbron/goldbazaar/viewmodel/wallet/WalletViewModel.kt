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

class WalletViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

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
    val selectedAsset: MutableLiveData<String?> get() = _selectedAsset

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
                // Load gold price data from market repository
                val pricesResult = marketRepository.getDailyPrices()
                val percentagesResult = marketRepository.getDailyPercentages()

                val marketPrices = if (pricesResult is com.mulosbron.goldbazaar.service.network.NetworkResult.Success) {
                    pricesResult.data
                } else {
                    emptyMap()
                }

                val marketPercentages = if (percentagesResult is com.mulosbron.goldbazaar.service.network.NetworkResult.Success) {
                    percentagesResult.data
                } else {
                    emptyMap()
                }

                _goldPrices.value = marketPrices
                _dailyPercentages.value = marketPercentages

                // Now load wallet data
                val allTransactions = repository.getAllTransactions()
                val assetList = repository.getUniqueAssets()

                val avgPrices = mutableMapOf<String, Double>()
                val profitMap = mutableMapOf<String, Double>()

                assetList.forEach { asset ->
                    avgPrices[asset] = repository.calculateAverageBuyingPrice(asset)

                    // Use actual market price for profit calculation if available
                    val currentPrice = marketPrices[asset]?.sellingPrice?.toDouble() ?: 0.0
                    profitMap[asset] = repository.calculateProfit(asset, currentPrice)
                }

                withContext(Dispatchers.Main) {
                    _transactions.value = allTransactions
                    _assets.value = assetList
                    _averagePrices.value = avgPrices
                    _profits.value = profitMap
                    _uiState.value = WalletUiState.Success
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = WalletUiState.Error(e.message ?: "Bilinmeyen bir hata oluştu")
                }
            }
        }
    }

    fun loadTransactionsForAsset(asset: String) {
        _uiState.value = WalletUiState.Loading
        _selectedAsset.value = asset

        ioScope.launch {
            try {
                val assetTransactions = repository.getTransactionsByAsset(asset)
                withContext(Dispatchers.Main) {
                    _selectedAssetTransactions.value = assetTransactions
                    _uiState.value = WalletUiState.Success
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = WalletUiState.Error(e.message ?: "İşlemler yüklenirken bir hata oluştu")
                }
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        _uiState.value = WalletUiState.Loading

        ioScope.launch {
            try {
                repository.addTransaction(transaction)

                if (_selectedAsset.value == transaction.asset) {
                    val assetTransactions = repository.getTransactionsByAsset(transaction.asset)
                    withContext(Dispatchers.Main) {
                        _selectedAssetTransactions.value = assetTransactions
                    }
                }

                loadData()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = WalletUiState.Error(e.message ?: "İşlem eklenirken bir hata oluştu")
                }
            }
        }
    }

    fun updateTransaction(transactionId: String, updatedTransaction: Transaction) {
        _uiState.value = WalletUiState.Loading

        ioScope.launch {
            try {
                repository.updateTransaction(transactionId, updatedTransaction)

                if (_selectedAsset.value == updatedTransaction.asset) {
                    val assetTransactions = repository.getTransactionsByAsset(updatedTransaction.asset)
                    withContext(Dispatchers.Main) {
                        _selectedAssetTransactions.value = assetTransactions
                    }
                }

                loadData()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = WalletUiState.Error(e.message ?: "İşlem güncellenirken bir hata oluştu")
                }
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        _uiState.value = WalletUiState.Loading

        ioScope.launch {
            try {
                val transactionToDelete = repository.getAllTransactions().find { it.id == transactionId }
                val assetOfTransaction = transactionToDelete?.asset

                repository.deleteTransaction(transactionId)

                if (_selectedAsset.value == assetOfTransaction) {
                    val assetTransactions = repository.getTransactionsByAsset(assetOfTransaction!!)
                    withContext(Dispatchers.Main) {
                        _selectedAssetTransactions.value = assetTransactions
                    }
                }

                loadData()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = WalletUiState.Error(e.message ?: "İşlem silinirken bir hata oluştu")
                }
            }
        }
    }

    fun deleteAsset(asset: String) {
        _uiState.value = WalletUiState.Loading

        ioScope.launch {
            try {
                repository.deleteAssetTransactions(asset)

                // Clear selected asset transactions if this was the selected asset
                if (_selectedAsset.value == asset) {
                    withContext(Dispatchers.Main) {
                        _selectedAssetTransactions.value = emptyList()
                        _selectedAsset.value = null
                    }
                }

                // Reload all data to update summaries
                loadData()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = WalletUiState.Error(e.message ?: "Varlık silinirken bir hata oluştu")
                }
            }
        }
    }

    // Get available assets for adding new transactions
    fun getAvailableAssets(): List<String> {
        return _goldPrices.value?.keys?.toList() ?: emptyList()
    }

    // Get current price for an asset (for pre-filling the price field)
    fun getCurrentPrice(asset: String, isBuying: Boolean): Double {
        val prices = _goldPrices.value?.get(asset)
        return if (isBuying) {
            prices?.buyingPrice?.toDouble() ?: 0.0
        } else {
            prices?.sellingPrice?.toDouble() ?: 0.0
        }
    }

    fun calculateTotalPortfolioValue(): Double {
        var totalValue = 0.0
        _assets.value?.forEach { asset ->
            val transactions = repository.getTransactionsByAsset(asset)
            var netAmount = 0.0

            transactions.forEach { transaction ->
                if (transaction.transactionType == "buy") {
                    netAmount += transaction.amount
                } else if (transaction.transactionType == "sell") {
                    netAmount -= transaction.amount
                }
            }

            val currentPrice = _goldPrices.value?.get(asset)?.sellingPrice?.toDouble() ?: 0.0
            totalValue += netAmount * currentPrice
        }

        return totalValue
    }
}