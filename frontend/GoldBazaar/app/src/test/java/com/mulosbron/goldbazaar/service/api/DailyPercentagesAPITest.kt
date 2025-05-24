package com.mulosbron.goldbazaar.service.api

import com.google.gson.GsonBuilder
import com.mulosbron.goldbazaar.model.remote.ApiResponse
import com.mulosbron.goldbazaar.model.remote.DailyPctPayload
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DailyPercentagesAPITest {

    private lateinit var server: MockWebServer
    private lateinit var api: DailyPercentagesAPI

    @Before
    fun setup() {
        server = MockWebServer().apply { start() }

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()

        api = retrofit.create(DailyPercentagesAPI::class.java)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    /**
     * /api/gold-daily-percentages/latest başarı senaryosu
     */
    @Test
    fun `Daily percentages endpoint returns valid payload`() = runBlocking {
        /* ---------- Arrange ---------- */
        val body = """
            {
              "success": true,
              "message": null,
              "data": {
                "date": "2025-05-02",
                "percentageDifference": {
                  "Gram Altın": { "buyingPrice": 9.43243243243243, "sellingPrice": 9.43243243243243 }
                }
              }
            }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        /* ---------- Act ---------- */
        val response: ApiResponse<DailyPctPayload> = api.getLatestDailyPercentages()

        /* ---------- Assert ---------- */
        assertTrue(response.success)
        assertNull(response.message)

        val payload = response.data
        assertNotNull(payload)
        assertEquals("2025-05-02", payload.date)

        // Map alanı: "percentageDifference"
        assertTrue(payload.percentageDifference.containsKey("Gram Altın"))
        val gram = payload.percentageDifference.getValue("Gram Altın")
        assertEquals(9.43243243243243, gram.buyingPrice, 0.0000001)
        assertEquals(9.43243243243243, gram.sellingPrice, 0.0000001)

        // Sunucuya tek istek gitti mi?
        assertEquals(1, server.requestCount)
    }
}
