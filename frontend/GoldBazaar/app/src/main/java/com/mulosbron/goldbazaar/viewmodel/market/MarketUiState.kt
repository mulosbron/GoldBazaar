package com.mulosbron.goldbazaar.viewmodel.market

sealed class MarketUiState {
    data object Idle : MarketUiState()
    data object Loading : MarketUiState()
    data object Success : MarketUiState()
    data class Error(val message: String) : MarketUiState()
}