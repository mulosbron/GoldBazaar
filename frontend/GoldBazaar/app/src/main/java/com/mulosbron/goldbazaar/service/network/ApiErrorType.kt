package com.mulosbron.goldbazaar.service.network

/**
 * API hatalarını sınıflandırmak için kullanılan enum sınıfı.
 */
enum class ApiErrorType {
    // Ağ hataları
    NETWORK_ERROR,         // İnternet bağlantısı sorunları
    TIMEOUT,               // İstek zaman aşımına uğradı

    // Sunucu hataları
    SERVER_ERROR,          // 500 hataları
    MAINTENANCE,           // Sunucu bakımda

    // Kimlik doğrulama hataları
    UNAUTHORIZED,          // 401 - Token geçersiz/eksik
    FORBIDDEN,             // 403 - Yetki yok
    TOKEN_EXPIRED,         // Token süresi doldu

    // İçerik hataları
    NOT_FOUND,             // 404 - Kaynak bulunamadı
    VALIDATION_ERROR,      // Gönderilen veriler geçersiz
    INVALID_REQUEST,       // İstek formatı geçersiz

    // İş mantığı hataları
    INSUFFICIENT_FUNDS,    // Yetersiz bakiye (altın alımı için)
    DUPLICATE_ENTRY,       // Zaten var olan bir kayıt
    RATE_LIMIT_EXCEEDED,   // API kullanım limiti aşıldı

    // Diğer
    UNKNOWN                // Sınıflandırılamayan hatalar
}