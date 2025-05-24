package com.mulosbron.goldbazaar.service.api

import com.mulosbron.goldbazaar.model.entity.NewsArticle
import com.mulosbron.goldbazaar.model.remote.ApiResponse
import com.mulosbron.goldbazaar.model.remote.NewsArticleRemote
import com.mulosbron.goldbazaar.model.response.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {
//    @GET("api/gold-news")
//    suspend fun getGoldNews(
//        @Query("sortBy") sortBy: String = "publishedAt",
//        @Query("pageSize") pageSize: Int = 20,
//        @Query("page") page: Int = 1
//    ): NewsResponse
    @GET("api/gold-news")
    suspend fun getGoldNews(
        @Query("sortBy")   sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int  = 20,
        @Query("page")     page: Int      = 1
    ): ApiResponse<List<NewsArticleRemote>>
}