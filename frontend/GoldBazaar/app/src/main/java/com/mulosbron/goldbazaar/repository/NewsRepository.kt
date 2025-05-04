package com.mulosbron.goldbazaar.repository

import com.mulosbron.goldbazaar.model.response.NewsResponse
import com.mulosbron.goldbazaar.repository.interfaces.INewsRepository
import com.mulosbron.goldbazaar.service.api.NewsAPI
import com.mulosbron.goldbazaar.service.network.NetworkResult
import com.mulosbron.goldbazaar.util.ext.safeApiCall

class NewsRepository(
    private val newsAPI: NewsAPI
) : INewsRepository {

    override suspend fun getGoldNews(
        sortBy: String,
        pageSize: Int,
        page: Int
    ): NetworkResult<NewsResponse> {
        return safeApiCall {
            newsAPI.getGoldNews(sortBy, pageSize, page)
        }
    }
}