package com.mulosbron.goldbazaar.repository.interfaces

import com.mulosbron.goldbazaar.model.response.NewsResponse
import com.mulosbron.goldbazaar.service.network.NetworkResult

/**
 * Haber verileri için repository arayüzü.
 * Altın piyasası haberleri için veri erişimi sağlar.
 */
interface INewsRepository {
    /**
     * Güncel altın haberlerini getirir.
     * @param sortBy Sıralama kriteri (örn: "publishedAt", "relevancy", "popularity")
     * @param pageSize Sayfa başına haber sayısı
     * @param page Sayfa numarası
     * @return Haber listesi ve meta bilgilerini içeren response
     */
    suspend fun getGoldNews(
        sortBy: String = "publishedAt",
        pageSize: Int = 20,
        page: Int = 1
    ): NetworkResult<NewsResponse>
}