package com.mulosbron.goldbazaar.service.api

import com.google.gson.GsonBuilder
import com.mulosbron.goldbazaar.model.remote.ApiResponse
import com.mulosbron.goldbazaar.model.remote.PricePayload
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DailyPricesAPITest {

    private lateinit var server: MockWebServer
    private lateinit var api: DailyPricesAPI

    @Before
    fun setup() {
        server = MockWebServer().apply { start() }

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))           // MockWebServer URL
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()

        api = retrofit.create(DailyPricesAPI::class.java)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    /**
     * /api/gold-prices/latest normal başarı senaryosu
     */
    @Test
    fun `Gold prices endpoint returns valid payload`() = runBlocking {
        /* ---------- Arrange ---------- */
        val body = """
            {
              "success": true,
              "message": null,
              "data": {
                "date": "2025-05-02",
                "data": {
                  "Gram Altın": { "buyingPrice": 4049, "sellingPrice": 4049 }
                }
              }
            }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        /* ---------- Act ---------- */
        val response: ApiResponse<PricePayload> = api.getLatestGoldPrices()

        /* ---------- Assert ---------- */
        // 1) Genel alanlar
        assertTrue(response.success)
        assertNull(response.message)

        // 2) Ana data objesi
        val payload = response.data
        assertNotNull(payload)
        assertEquals("2025-05-02", payload.date)

        // 3) Map içeriği
        assertTrue(payload.data.containsKey("Gram Altın"))
        val gram = payload.data.getValue("Gram Altın")
        assertEquals(4049.0, gram.buyingPrice, 0.0)
        assertEquals(4049.0, gram.sellingPrice, 0.0)

        // 4) Sunucuya gerçekten 1 istek atıldı mı?
        assertEquals(1, server.requestCount)
    }
}
