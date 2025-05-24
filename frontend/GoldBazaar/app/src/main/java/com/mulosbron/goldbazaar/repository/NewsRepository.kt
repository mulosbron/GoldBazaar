package com.mulosbron.goldbazaar.repository

import com.mulosbron.goldbazaar.model.entity.NewsArticle
import com.mulosbron.goldbazaar.model.entity.NewsSource
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
    ): NetworkResult<NewsResponse> = safeApiCall {

        val resp = newsAPI.getGoldNews(sortBy, pageSize, page)

        if (!resp.success)
            throw IllegalStateException(resp.message ?: "Unknown error")

        // 🔄  List<NewsArticleRemote>  ➜  List<NewsArticle>
        val mappedArticles = resp.data.map { r ->
            NewsArticle(
                id          = "",                         // sunucu artık göndermiyor
                source      = NewsSource(r.sourceName),
                author      = r.author,
                title       = r.title,
                description = r.description,
                url         = r.url,
                urlToImage  = r.urlToImage,
                publishedAt = r.publishedAt,
                content     = null,                       // alan yok
                scrapedAt   = ""                          // alan yok
            )
        }

        NewsResponse(
            status        = if (resp.success) "ok" else "error",
            totalResults  = mappedArticles.size,
            articles      = mappedArticles,
            success       = resp.success,
            message       = resp.message ?: ""
        )
    }


//    override suspend fun getGoldNews(
//        sortBy: String,
//        pageSize: Int,
//        page: Int
//    ): NetworkResult<NewsResponse> {
//        return safeApiCall {
//            newsAPI.getGoldNews(sortBy, pageSize, page)
//        }
//    }
}