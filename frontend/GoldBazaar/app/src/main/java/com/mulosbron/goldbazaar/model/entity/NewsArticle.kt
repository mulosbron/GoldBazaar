package com.mulosbron.goldbazaar.model.entity

data class NewsArticle(
    val id: String,
    val source: NewsSource,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?,
    val scrapedAt: String
) {
    fun getShortDescription(): String {
        return description?.let { desc ->
            if (desc.length > 150) {
                desc.take(147) + "..."
            } else {
                desc
            }
        } ?: ""
    }

    fun getFormattedSource(): String {
        val formattedTime = try {
            val inputFormat = java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                java.util.Locale.getDefault()
            )
            val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val date = inputFormat.parse(publishedAt)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }

        return if (formattedTime.isNotEmpty()) {
            "${source.name} • $formattedTime"
        } else {
            source.name
        }
    }
}