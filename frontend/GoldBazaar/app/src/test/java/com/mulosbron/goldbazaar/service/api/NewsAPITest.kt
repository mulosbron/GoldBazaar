package com.mulosbron.goldbazaar.service.api

import com.google.gson.GsonBuilder
import com.mulosbron.goldbazaar.model.remote.ApiResponse
import com.mulosbron.goldbazaar.model.remote.NewsArticleRemote
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsAPITest {

    private lateinit var server: MockWebServer
    private lateinit var api: NewsAPI

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

        api = retrofit.create(NewsAPI::class.java)
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    /**
     * /api/gold-news başarı senaryosu
     */
    @Test
    fun `Gold news endpoint returns list of articles`() = runBlocking {
        /* ---------- Arrange ---------- */
        val body = """
            {
              "success": true,
              "message": null,
              "data": [
                {
                  "title": "Ünlü bankadan altın için yeni tahmin geldi",
                  "url": "https://example.com/altin-tahmini",
                  "author": "Finans Haber",
                  "description": "Altın rekor kırabilir…",
                  "urlToImage": "https://example.com/img1.jpg",
                  "publishedAt": "2025-04-30T19:00:15Z",
                  "sourceName": "Example.com"
                },
                {
                  "title": "Gram altında trend kırılacak",
                  "url": "https://example.com/gram-altin",
                  "author": "Ekonomi Yazarı",
                  "description": "Uzmanlar kritik eşiğe dikkat çekti…",
                  "urlToImage": "https://example.com/img2.jpg",
                  "publishedAt": "2025-04-30T18:25:00Z",
                  "sourceName": "Ekonomi.com"
                }
              ]
            }
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        /* ---------- Act ---------- */
        val response: ApiResponse<List<NewsArticleRemote>> = api.getGoldNews()

        /* ---------- Assert ---------- */
        assertTrue(response.success)
        assertNull(response.message)

        val articles = response.data
        assertNotNull(articles)
        assertEquals(2, articles.size)

        // İlk haberi kontrol et
        val first = articles[0]
        assertEquals("Ünlü bankadan altın için yeni tahmin geldi", first.title)
        assertEquals("https://example.com/altin-tahmini", first.url)
        assertEquals("Finans Haber", first.author)
        assertEquals("Example.com", first.sourceName)
        assertEquals("2025-04-30T19:00:15Z", first.publishedAt)

        // Sunucuya tek istek atıldı mı?
        assertEquals(1, server.requestCount)
    }
}
