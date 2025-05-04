"""
Doğrulama modülü - Veri yapılarının doğrulanmasını sağlar.
"""
from utils.logger import setup_logger

logger = setup_logger(__name__)


def validate_price_data(prices_data):
    """
    Altın fiyat verilerinin doğru formatta olup olmadığını kontrol eder.

    Args:
        prices_data (dict): Kontrol edilecek fiyat verileri.

    Returns:
        (bool, str): Kontrol sonucu ve hata mesajı (başarılı ise None).
    """
    if not prices_data:
        return False, "Fiyat verisi boş."

    if not isinstance(prices_data, dict):
        return False, f"Fiyat verisi bir sözlük olmalıdır: {type(prices_data)}"

    if len(prices_data) == 0:
        return False, "Fiyat verisi boş sözlük."

    # Alt öğelerin yapısını kontrol et
    for gold_type, price_info in prices_data.items():
        if not isinstance(gold_type, str):
            return False, f"Altın türü bir string olmalıdır: {type(gold_type)}"

        if not isinstance(price_info, dict):
            return False, f"Fiyat bilgisi ({gold_type}) bir sözlük olmalıdır: {type(price_info)}"

        if "Alış Fiyatı" not in price_info or "Satış Fiyatı" not in price_info:
            return False, f"Fiyat bilgisi ({gold_type}) 'Alış Fiyatı' ve 'Satış Fiyatı' içermelidir."

        if not isinstance(price_info["Alış Fiyatı"], (int, float)) or not isinstance(price_info["Satış Fiyatı"],
                                                                                     (int, float)):
            return False, f"Fiyat değerleri ({gold_type}) sayısal olmalıdır."

    logger.info(f"Fiyat verisi doğrulandı: {len(prices_data)} farklı altın türü içeriyor.")
    return True, None


def validate_percentage_data(percentage_data):
    """
    Yüzde değişim verilerinin doğru formatta olup olmadığını kontrol eder.

    Args:
        percentage_data (dict): Kontrol edilecek yüzde değişim verileri.

    Returns:
        (bool, str): Kontrol sonucu ve hata mesajı (başarılı ise None).
    """
    if not percentage_data:
        return False, "Yüzde değişim verisi boş."

    if not isinstance(percentage_data, dict):
        return False, f"Yüzde değişim verisi bir sözlük olmalıdır: {type(percentage_data)}"

    return True, None