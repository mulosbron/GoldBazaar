"""
Yüzde hesaplama modülü - Altın fiyatlarındaki günlük değişimleri hesaplar.
"""
import numpy as np
import pandas as pd
from datetime import datetime, timedelta

import config
from utils.logger import setup_logger, log_function_call
from utils.db_handler import DatabaseHandler

logger = setup_logger(__name__)


class PercentageCalculator:
    """
    Altın fiyatlarındaki yüzde değişimlerini hesaplayan sınıf.
    """

    def __init__(self, db_handler=None):
        """
        Args:
            db_handler (DatabaseHandler, optional): Veritabanı işlemlerini yönetecek nesne.
        """
        self.db_handler = db_handler or DatabaseHandler()

    @log_function_call
    def find_documents_for_comparison(self):
        """
        Karşılaştırma için gereken belgeleri bulur: en son belge ve bir önceki gün.

        Returns:
            tuple: (en_son_belge, onceki_gun_belgesi) çifti. Bulunamazsa None değer içerebilir.
        """
        logger.info("Karşılaştırma için belgeler aranıyor...")

        # En son belgeyi al
        latest_document = self.db_handler.get_latest_gold_prices()
        if not latest_document:
            logger.warning("En son eklenen belge bulunamadı.")
            return None, None

        # Önceki günün tarihini hesapla
        latest_date = datetime.strptime(latest_document['date'], config.DATE_FORMAT)
        related_date = latest_date - timedelta(days=1)
        related_date_str = related_date.strftime(config.DATE_FORMAT)

        # Önceki günün belgesini al
        related_document = self.db_handler.get_gold_prices_by_date(related_date_str)

        if related_document:
            logger.info(f"İlgili belge bulundu: {related_date_str}")
        else:
            logger.warning(f"İlgili belge bulunamadı: {related_date_str}")

        return latest_document, related_document

    @staticmethod
    @log_function_call
    def convert_to_dataframe(document):
        """
        MongoDB belgesini pandas DataFrame'e dönüştürür.

        Args:
            document (dict): MongoDB belgesi.

        Returns:
            pandas.DataFrame: Dönüştürülmüş DataFrame.
        """
        if not document or 'data' not in document:
            return pd.DataFrame()

        return pd.DataFrame(document['data'])

    @staticmethod
    @log_function_call
    def calculate_percentage_diff(df_base, df_compare):
        """
        İki DataFrame arasındaki yüzde değişimi hesaplar.

        Args:
            df_base (pandas.DataFrame): Baz alınacak DataFrame (önceki gün).
            df_compare (pandas.DataFrame): Karşılaştırılacak DataFrame (güncel).

        Returns:
            pandas.DataFrame: Yüzde değişim DataFrame'i.
        """
        logger.info("DataFrames arasında yüzde değişimi hesaplanıyor...")

        if df_base.empty or df_compare.empty:
            logger.warning("Boş DataFrame ile yüzde hesaplaması yapılamaz.")
            return pd.DataFrame()

        def percentage_diff(x1, x2):
            """Yüzde değişim hesaplama fonksiyonu"""
            return np.where(
                (x1 != 0) & ~np.isnan(x1) & ~np.isnan(x2),
                (x2 - x1) / x1 * 100,
                np.nan
            )

        # İki DataFrame'i birleştir ve yüzde değişimi hesapla
        result = df_base.combine(df_compare, func=percentage_diff)

        logger.info("Yüzde değişim hesaplaması tamamlandı.")
        return result

    @log_function_call
    def process_daily_differences(self):
        """
        Günlük fiyat değişimlerini hesaplar ve veritabanına kaydeder.

        Returns:
            pandas.DataFrame or None: Yüzde değişim DataFrame'i. İşlem başarısızsa None.
        """
        try:
            # Karşılaştırma belgelerini al
            latest_doc, previous_doc = self.find_documents_for_comparison()
            if not latest_doc or not previous_doc:
                logger.warning("Karşılaştırma için gerekli belgeler bulunamadığından işlem iptal edildi.")
                return None

            # DataFrame'e dönüştür
            df_previous = self.convert_to_dataframe(previous_doc)
            df_latest = self.convert_to_dataframe(latest_doc)

            # Yüzde değişimi hesapla
            percentage_diff = self.calculate_percentage_diff(df_previous, df_latest)

            # Sonuçları kaydet
            if not percentage_diff.empty:
                percentage_dict = percentage_diff.to_dict()
                self.db_handler.insert_percentage_differences(latest_doc['date'], percentage_dict)
                logger.info(f"Yüzde değişim verileri '{latest_doc['date']}' için kaydedildi.")
                return percentage_diff
            else:
                logger.warning("Yüzde değişim hesaplaması sonucu boş, kayıt yapılmadı.")
                return None

        except Exception as e:
            logger.error(f"Yüzde değişim hesaplaması sırasında hata: {str(e)}")
            return None