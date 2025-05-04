package com.mulosbron.goldbazaar.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.mulosbron.goldbazaar.R
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object SimpleImageLoader {
    // Görsel yükleme için bir executor havuzu
    private val executor: Executor = Executors.newFixedThreadPool(5)
    private val mainHandler = Handler(Looper.getMainLooper())

    // Görsel önbelleği (basit bir in-memory cache)
    private val imageCache = mutableMapOf<String, Bitmap>()

    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        placeholderResId: Int = R.drawable.ic_image,
        errorResId: Int = R.drawable.ic_broken_image
    ) {
        // Eğer URL null veya boş ise, hata resmini göster
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(errorResId)
            return
        }

        // Yükleme başlamadan önce placeholder göster
        imageView.setImageResource(placeholderResId)

        // Önbellekte görsel var mı kontrol et
        val cachedBitmap = imageCache[imageUrl]
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        // Arkaplan thread'inde görseli yükle
        executor.execute {
            loadImageFromUrl(imageUrl)?.let { loadedBitmap ->
                // UI thread'inde ImageView'ı güncelle
                mainHandler.post {
                    imageView.setImageBitmap(loadedBitmap)
                }
            } ?: run {
                // Yükleme başarısız olduysa hata resmini göster
                mainHandler.post {
                    imageView.setImageResource(errorResId)
                }
            }
        }
    }

    private fun loadImageFromUrl(imageUrl: String): Bitmap? {
        var connection: HttpURLConnection? = null

        try {
            // Önbellekte varsa oradan döndür
            imageCache[imageUrl]?.let {
                return it
            }

            val url = URL(imageUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Bitmap başarıyla yüklendiyse önbelleğe ekle
            bitmap?.let {
                synchronized(imageCache) {
                    imageCache[imageUrl] = it
                }
            }

            return bitmap
        } catch (e: Exception) {
            // Hata durumunda null döndür
            return null
        } finally {
            connection?.disconnect()
        }
    }
}