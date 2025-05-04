package com.mulosbron.goldbazaar.viewmodel.market

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mulosbron.goldbazaar.model.entity.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.entity.DailyGoldPrice
import com.mulosbron.goldbazaar.repository.interfaces.IMarketRepository
import com.mulosbron.goldbazaar.service.network.NetworkResult
import com.mulosbron.goldbazaar.util.StringProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarketViewModel(
    private val marketRepository: IMarketRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _marketUiState = MutableLiveData<MarketUiState>(MarketUiState.Idle)
    val marketUiState: LiveData<MarketUiState> = _marketUiState

    private val _goldPrices = MutableLiveData<Map<String, DailyGoldPrice>>(emptyMap())
    val goldPrices: LiveData<Map<String, DailyGoldPrice>> = _goldPrices

    private val _dailyPercentages = MutableLiveData<Map<String, DailyGoldPercentage>>(emptyMap())
    val dailyPercentages: LiveData<Map<String, DailyGoldPercentage>> = _dailyPercentages

    private val _lastUpdated = MutableLiveData<String>()
    val lastUpdated: LiveData<String> = _lastUpdated

    fun fetchMarketData() {
        _marketUiState.value = MarketUiState.Loading

        viewModelScope.launch {
            try {
                // async/await kullanarak paralel istekleri başlat
                val pricesDeferred = async { marketRepository.getDailyPrices() }
                val percentagesDeferred = async { marketRepository.getDailyPercentages() }

                // Paralel isteklerin sonuçlarını bekle
                val pricesResult = pricesDeferred.await()
                val percentagesResult = percentagesDeferred.await()

                // İlk olarak fiyat verilerini işle
                when (pricesResult) {
                    is NetworkResult.Success -> {
                        _goldPrices.value = pricesResult.data

                        // Şimdi de yüzde verilerini işle
                        when (percentagesResult) {
                            is NetworkResult.Success -> {
                                _dailyPercentages.value = percentagesResult.data

                                // Son güncelleme zamanını ayarla
                                _lastUpdated.value =
                                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                        .format(Date())

                                _marketUiState.value = MarketUiState.Success
                            }

                            is NetworkResult.Error -> {
                                _marketUiState.value =
                                    MarketUiState.Error(percentagesResult.errorMessage)
                            }

                            is NetworkResult.Loading -> {
                                _marketUiState.value = MarketUiState.Loading
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        _marketUiState.value = MarketUiState.Error(pricesResult.errorMessage)
                    }

                    is NetworkResult.Loading -> {
                        _marketUiState.value = MarketUiState.Loading
                    }
                }
            } catch (e: Exception) {
                _marketUiState.value =
                    MarketUiState.Error(e.message ?: stringProvider.getUnknownString())
            }
        }
    }

    // Verileri yenileme
    fun refreshData() {
        viewModelScope.launch {
            try {
                // async/await kullanarak yenileme işlemini gerçekleştir
                async { marketRepository.refreshAllData() }.await()
                fetchMarketData()
            } catch (e: Exception) {
                _marketUiState.value =
                    MarketUiState.Error(e.message ?: stringProvider.getUnknownString())
            }
        }
    }
}

