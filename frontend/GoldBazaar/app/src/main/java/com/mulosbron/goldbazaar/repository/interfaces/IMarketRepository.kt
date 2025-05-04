package com.mulosbron.goldbazaar.repository.interfaces

import com.mulosbron.goldbazaar.model.entity.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.entity.DailyGoldPrice
import com.mulosbron.goldbazaar.service.network.NetworkResult

/**
 * Piyasa verileri için repository arayüzü.
 * Altın fiyatları ve yüzdelik değişimler için veri erişimi sağlar.
 */
interface IMarketRepository {
    /**
     * Tüm günlük altın fiyatlarını getirir.
     * @return Altın tipi -> Fiyat bilgisi eşleşmesi
     */
    suspend fun getDailyPrices(): NetworkResult<Map<String, DailyGoldPrice>>

    /**
     * Tüm günlük yüzdelik değişimleri getirir.
     * @return Altın tipi -> Yüzdelik değişim eşleşmesi
     */
    suspend fun getDailyPercentages(): NetworkResult<Map<String, DailyGoldPercentage>>

    /**
     * Belirli bir altın türü için fiyat bilgisi getirir.
     * @param productName Altın türü ismi
     * @return Altın fiyat bilgisi
     */
    suspend fun getGoldPriceByName(productName: String): NetworkResult<DailyGoldPrice>

    /**
     * Belirli bir altın türü için yüzdelik değişim bilgisi getirir.
     * @param productName Altın türü ismi
     * @return Altın yüzdelik değişim bilgisi
     */
    suspend fun getGoldPercentageByName(productName: String): NetworkResult<DailyGoldPercentage>

    /**
     * Tüm önbelleği temizleyerek verileri yeniler.
     */
    suspend fun refreshAllData()
}