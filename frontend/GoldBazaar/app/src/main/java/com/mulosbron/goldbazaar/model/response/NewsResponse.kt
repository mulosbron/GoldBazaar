package com.mulosbron.goldbazaar.model.response

import com.mulosbron.goldbazaar.model.entity.NewsArticle
import com.mulosbron.goldbazaar.util.ext.capitalize

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsArticle>,
    override val message: String = "",
    override val success: Boolean = false
) : BaseApiResponse {

    override fun isSuccessful(): Boolean {
        return status.equals("ok", ignoreCase = true) && articles.isNotEmpty()
    }

    override fun getFormattedMessage(): String {
        return when {
            message.isNotEmpty() -> message.replace("_", " ").capitalize()
            !isSuccessful() -> "No articles found"
            else -> "Successfully loaded ${articles.size} articles"
        }
    }
}