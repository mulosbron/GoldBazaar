package com.mulosbron.goldbazaar.repository

import com.mulosbron.goldbazaar.model.entity.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.entity.DailyGoldPrice
import com.mulosbron.goldbazaar.repository.interfaces.IMarketRepository
import com.mulosbron.goldbazaar.service.api.DailyPercentagesAPI
import com.mulosbron.goldbazaar.service.api.DailyPricesAPI
import com.mulosbron.goldbazaar.service.network.ApiErrorType
import com.mulosbron.goldbazaar.service.network.NetworkResult
import com.mulosbron.goldbazaar.util.NetworkConstants
import com.mulosbron.goldbazaar.util.SharedPrefsManager
import com.mulosbron.goldbazaar.util.ext.safeApiCall

class MarketRepository(
    private val goldPricesAPI: DailyPricesAPI,
    private val dailyPercentagesAPI: DailyPercentagesAPI,
    private val sharedPrefsManager: SharedPrefsManager
) : IMarketRepository {

    // Altın fiyatları için önbellek
    private var cachedDailyPrices: Map<String, DailyGoldPrice>? = null

    // Yüzdelik değişimler için önbellek
    private var cachedDailyPercentages: Map<String, DailyGoldPercentage>? = null

    override suspend fun getDailyPrices(): NetworkResult<Map<String, DailyGoldPrice>> {
        val currentTime = System.currentTimeMillis()
        val lastFetchTime = sharedPrefsManager.getDailyPricesLastFetchTime()

        // Önbellek geçerli mi kontrol et
        if (cachedDailyPrices != null &&
            currentTime - lastFetchTime < NetworkConstants.CACHE_VALIDITY_DURATION
        ) {
            return NetworkResult.success(cachedDailyPrices!!)
        }

        // Önbellek geçerli değilse API'den getir
        return safeApiCall {
            val freshData = goldPricesAPI.getLatestGoldPricesSuspend()
            cachedDailyPrices = freshData
            sharedPrefsManager.saveDailyPricesLastFetchTime(currentTime)
            freshData
        }
    }

    override suspend fun getDailyPercentages(): NetworkResult<Map<String, DailyGoldPercentage>> {
        val currentTime = System.currentTimeMillis()
        val lastFetchTime = sharedPrefsManager.getDailyPercentagesLastFetchTime()

        // Önbellek geçerli mi kontrol et
        if (cachedDailyPercentages != null &&
            currentTime - lastFetchTime < NetworkConstants.CACHE_VALIDITY_DURATION
        ) {
            return NetworkResult.success(cachedDailyPercentages!!)
        }

        // Önbellek geçerli değilse API'den getir
        return safeApiCall {
            val freshData = dailyPercentagesAPI.getLatestDailyPercentagesSuspend()
            cachedDailyPercentages = freshData
            sharedPrefsManager.saveDailyPercentagesLastFetchTime(currentTime)
            freshData
        }
    }

    override suspend fun getGoldPriceByName(productName: String): NetworkResult<DailyGoldPrice> {
        // Önce önbellekte var mı kontrol et
        val cachedPrices = cachedDailyPrices
        if (cachedPrices != null && cachedPrices.containsKey(productName)) {
            return NetworkResult.success(cachedPrices[productName]!!)
        }

        // Önbellekte yoksa tüm verileri getir
        return when (val pricesResult = getDailyPrices()) {
            is NetworkResult.Success -> {
                val prices = pricesResult.data
                if (prices.containsKey(productName)) {
                    NetworkResult.success(prices[productName]!!)
                } else {
                    NetworkResult.error(
                        errorType = ApiErrorType.NOT_FOUND,
                        message = "Bu ürün için fiyat bilgisi bulunamadı."
                    )
                }
            }

            is NetworkResult.Error -> pricesResult
            is NetworkResult.Loading -> NetworkResult.loading()
        }
    }

    override suspend fun getGoldPercentageByName(productName: String): NetworkResult<DailyGoldPercentage> {
        // Tüm yüzdeleri getir ve filtreleme işlemini burada yap
        // API çağrısı başarılı olduysa
        return when (val percentagesResult = getDailyPercentages()) {
            is NetworkResult.Success -> {
                val percentages = percentagesResult.data
                if (percentages.containsKey(productName)) {
                    NetworkResult.success(percentages[productName]!!)
                } else {
                    NetworkResult.error(
                        errorType = ApiErrorType.NOT_FOUND,
                        message = "Bu ürün için yüzdelik değişim bilgisi bulunamadı."
                    )
                }
            }

            is NetworkResult.Error -> percentagesResult
            is NetworkResult.Loading -> NetworkResult.loading()
        }
    }

    override suspend fun refreshAllData() {
        // Önbelleği temizle ve yeni veri getir
        cachedDailyPrices = null
        cachedDailyPercentages = null
        // Son güncelleme zamanlarını sıfırla
        sharedPrefsManager.clearMarketDataTimestamps()
        // Verileri tekrar getir
        getDailyPrices()
        getDailyPercentages()
    }
}