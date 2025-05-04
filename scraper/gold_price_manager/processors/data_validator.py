"""
Veri doğrulama modülü - Altın fiyat verilerinin tutarlılığını kontrol eder.
"""
from datetime import datetime

import config
from utils.logger import setup_logger, log_function_call
from utils.validators import validate_price_data
from utils.db_handler import DatabaseHandler

logger = setup_logger(__name__)


class DataValidator:
    """
    Altın fiyat verilerinin doğruluğunu ve tutarlılığını kontrol eden sınıf.
    """

    def __init__(self, db_handler=None):
        """
        Args:
            db_handler (DatabaseHandler, optional): Veritabanı işlemlerini yönetecek nesne.
        """
        self.db_handler = db_handler or DatabaseHandler()

    @log_function_call
    def validate_data_format(self, prices_data):
        """
        Veri formatının doğruluğunu kontrol eder.

        Args:
            prices_data (dict): Doğrulanacak fiyat verileri.

        Returns:
            bool: Veri formatı geçerliyse True, değilse False.
        """
        valid, error_message = validate_price_data(prices_data)
        if not valid:
            logger.error(f"Veri format doğrulaması başarısız: {error_message}")
        return valid

    @log_function_call
    def validate_data_consistency(self, prices_data):
        """
        Verilerin tutarlılığını kontrol eder (son verilerle karşılaştırarak).

        Args:
            prices_data (dict): Doğrulanacak fiyat verileri.

        Returns:
            bool: Veriler tutarlıysa True, değilse False.
        """
        # En son kaydedilen veriyi al
        latest_document = self.db_handler.get_latest_gold_prices()
        if not latest_document or 'data' not in latest_document:
            logger.warning("Tutarlılık kontrolü için karşılaştırılacak önceki veri bulunamadı.")
            return True  # Karşılaştırılacak veri yoksa geçerli say

        latest_data = latest_document['data']

        # Ürün sayısı kontrolü
        if len(prices_data) < len(latest_data) * 0.7:  # En az %70'i kadar ürün olmalı
            logger.error(f"Ürün sayısı tutarsızlığı: Yeni veri {len(prices_data)} ürün içeriyor, "
                         f"en son veri {len(latest_data)} ürün içeriyor.")
            return False

        # Ortak ürünlerde aşırı fiyat değişimi kontrolü
        inconsistent_products = []
        for product, prices in prices_data.items():
            if product in latest_data:
                old_buy = latest_data[product]['Alış Fiyatı']
                old_sell = latest_data[product]['Satış Fiyatı']
                new_buy = prices['Alış Fiyatı']
                new_sell = prices['Satış Fiyatı']

                # %30'dan fazla değişim varsa uyarı
                if (abs((new_buy - old_buy) / old_buy) > 0.3 or
                        abs((new_sell - old_sell) / old_sell) > 0.3):
                    inconsistent_products.append({
                        'product': product,
                        'old_buy': old_buy,
                        'new_buy': new_buy,
                        'old_sell': old_sell,
                        'new_sell': new_sell
                    })

        if inconsistent_products:
            logger.warning(f"Aşırı fiyat değişimi tespit edildi: {len(inconsistent_products)} üründe.")
            for item in inconsistent_products:
                logger.warning(f"Ürün: {item['product']}, "
                               f"Alış: {item['old_buy']} -> {item['new_buy']}, "
                               f"Satış: {item['old_sell']} -> {item['new_sell']}")

            # Aşırı değişim çok fazlaysa (toplam ürünlerin %20'sinden fazla)
            if len(inconsistent_products) > len(prices_data) * 0.2:
                logger.error("Çok sayıda üründe aşırı fiyat değişimi tespit edildi. "
                             "Veri tutarsız kabul ediliyor.")
                return False

        logger.info("Veri tutarlılık kontrolü başarılı.")
        return True

    @log_function_call
    def validate_complete_dataset(self, prices_data):
        """
        Tüm veri setinin geçerliliğini kontrol eder.

        Args:
            prices_data (dict): Doğrulanacak fiyat verileri.

        Returns:
            bool: Veriler geçerliyse True, değilse False.
        """
        # Format doğrulaması
        if not self.validate_data_format(prices_data):
            return False

        # Veri boşluk kontrolü
        if not prices_data:
            logger.error("Doğrulanacak veri boş.")
            return False

        # Tutarlılık kontrolü
        if not self.validate_data_consistency(prices_data):
            return False

        # Tüm kontroller başarılı
        logger.info("Tüm veri doğrulama kontrolleri başarılı: "
                    f"{len(prices_data)} altın türü için fiyat verisi geçerli.")
        return True