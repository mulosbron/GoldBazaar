package com.mulosbron.goldbazaar.viewmodel.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mulosbron.goldbazaar.model.entity.NewsArticle
import com.mulosbron.goldbazaar.repository.interfaces.INewsRepository
import com.mulosbron.goldbazaar.service.network.NetworkResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsViewModel(
    private val newsRepository: INewsRepository,
) : ViewModel() {

    private val _newsUiState = MutableLiveData<NewsUiState>(NewsUiState.Idle)
    val newsUiState: LiveData<NewsUiState> = _newsUiState

    private val _articles = MutableLiveData<List<NewsArticle>>(emptyList())
    val articles: LiveData<List<NewsArticle>> = _articles

    private val _lastUpdated = MutableLiveData<String>()
    val lastUpdated: LiveData<String> = _lastUpdated

    private var currentPage = 1
    private val pageSize = 20

    fun loadNews(sortBy: String = "publishedAt") {
        if (_newsUiState.value == NewsUiState.Loading) return

        _newsUiState.value = NewsUiState.Loading

        viewModelScope.launch {
            when (val result = newsRepository.getGoldNews(sortBy, pageSize, currentPage)) {
                is NetworkResult.Success -> {
                    val response = result.data

                    if (response.isSuccessful()) {
                        _articles.value = response.articles
                        _lastUpdated.value =
                            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                .format(Date())

                        if (response.articles.isEmpty()) {
                            _newsUiState.value = NewsUiState.Empty
                        } else {
                            _newsUiState.value = NewsUiState.Success
                        }
                    } else {
                        _newsUiState.value = NewsUiState.Error(response.getFormattedMessage())
                    }
                }

                is NetworkResult.Error -> {
                    _newsUiState.value = NewsUiState.Error(result.errorMessage)
                }

                is NetworkResult.Loading -> {
                    _newsUiState.value = NewsUiState.Loading
                }
            }
        }
    }

    fun refreshNews() {
        currentPage = 1
        loadNews()
    }
}