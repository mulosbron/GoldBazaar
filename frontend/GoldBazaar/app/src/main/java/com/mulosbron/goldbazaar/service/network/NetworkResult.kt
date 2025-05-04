package com.mulosbron.goldbazaar.service.network

/**
 * API yanıtlarını sarmak için kullanılan sealed class.
 * API çağrısı durumunu (başarılı, hata, yükleniyor) ve ilgili verileri içerir.
 */
sealed class NetworkResult<out T> {
    /**
     * Başarılı API yanıtı.
     * @param data API'dan dönen veri.
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()

    /**
     * Hata durumu.
     * @param errorType Hata tipi (enum).
     * @param errorMessage Hata mesajı.
     * @param errorCode HTTP hata kodu (isteğe bağlı).
     * @param errorData Hata ile ilgili ek veri (isteğe bağlı).
     */
    data class Error(
        val errorType: ApiErrorType,
        val errorMessage: String,
        val errorCode: Int? = null,
        val errorData: Any? = null
    ) : NetworkResult<Nothing>()

    /**
     * Yükleniyor durumu.
     */
    data object Loading : NetworkResult<Nothing>()

    companion object {
        /**
         * Başarılı bir NetworkResult oluşturur.
         */
        fun <T> success(data: T): NetworkResult<T> = Success(data)

        /**
         * Hata içeren bir NetworkResult oluşturur.
         * @param errorType Hata tipi
         * @param message Hata mesajı
         * @param errorCode İsteğe bağlı HTTP hata kodu
         * @param errorData İsteğe bağlı ek hata verisi
         */
        fun error(
            errorType: ApiErrorType,
            message: String,
            errorCode: Int? = null,
            errorData: Any? = null
        ): NetworkResult<Nothing> = Error(errorType, message, errorCode, errorData)

        /**
         * Önceden tanımlanmış hata mesajlarıyla NetworkResult oluşturan yardımcı fonksiyon
         * @param errorType Hata tipi
         * @param errorCode İsteğe bağlı HTTP hata kodu
         */
        fun standardError(errorType: ApiErrorType, errorCode: Int? = null): NetworkResult<Nothing> {
            val message = when (errorType) {
                ApiErrorType.NETWORK_ERROR -> "İnternet bağlantı hatası. Lütfen internet bağlantınızı kontrol edin."
                ApiErrorType.TIMEOUT -> "İstek zaman aşımına uğradı. Lütfen daha sonra tekrar deneyin."
                ApiErrorType.SERVER_ERROR -> "Sunucu hatası oluştu. Lütfen daha sonra tekrar deneyin."
                ApiErrorType.MAINTENANCE -> "Sunucu şu anda bakım modunda. Lütfen daha sonra tekrar deneyin."
                ApiErrorType.UNAUTHORIZED -> "Yetkilendirme hatası. Lütfen tekrar giriş yapın."
                ApiErrorType.FORBIDDEN -> "Bu işlem için yetkiniz yok."
                ApiErrorType.TOKEN_EXPIRED -> "Oturumunuz sona erdi. Lütfen tekrar giriş yapın."
                ApiErrorType.NOT_FOUND -> "İstenen kaynak bulunamadı."
                ApiErrorType.VALIDATION_ERROR -> "Gönderilen veriler geçersiz."
                ApiErrorType.INVALID_REQUEST -> "Geçersiz istek. Lütfen tekrar deneyin."
                ApiErrorType.INSUFFICIENT_FUNDS -> "Yetersiz bakiye."
                ApiErrorType.DUPLICATE_ENTRY -> "Bu kayıt zaten mevcut."
                ApiErrorType.RATE_LIMIT_EXCEEDED -> "API kullanım limitini aştınız. Lütfen daha sonra tekrar deneyin."
                ApiErrorType.UNKNOWN -> "Beklenmeyen bir hata oluştu."
            }
            return error(errorType, message, errorCode)
        }

        /**
         * Yükleniyor durumunda bir NetworkResult oluşturur.
         */
        fun <T> loading(): NetworkResult<T> = Loading
    }
}