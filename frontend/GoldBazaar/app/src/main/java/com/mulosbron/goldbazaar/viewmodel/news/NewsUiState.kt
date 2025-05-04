package com.mulosbron.goldbazaar.viewmodel.news

sealed class NewsUiState {
    data object Idle : NewsUiState()
    data object Loading : NewsUiState()
    data object Success : NewsUiState()
    data object Empty : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}