package com.mulosbron.goldbazaar.viewmodel.wallet

sealed class WalletUiState {
    data object Idle : WalletUiState()
    data object Loading : WalletUiState()
    data object Success : WalletUiState()
    data class Error(val message: String) : WalletUiState()
}