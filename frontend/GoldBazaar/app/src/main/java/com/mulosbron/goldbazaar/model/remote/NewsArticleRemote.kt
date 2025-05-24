package com.mulosbron.goldbazaar.model.remote

data class NewsArticleRemote(
    val title: String,
    val url: String,
    val author: String?,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val sourceName: String          // sunucunun alanı
)