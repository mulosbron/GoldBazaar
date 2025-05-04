"""
Veritabanı işlemleri modülü - MongoDB ile etkileşimi yönetir.
"""
from datetime import datetime
import time
from functools import wraps

from pymongo import MongoClient
from pymongo.errors import ConnectionFailure, ServerSelectionTimeoutError

import config
from utils.logger import setup_logger, log_function_call

logger = setup_logger(__name__)


def retry_on_mongodb_error(max_attempts=config.RETRY_ATTEMPTS, delay_seconds=config.RETRY_DELAY_SECONDS):
    """
    MongoDB hatalarına karşı yeniden deneme mekanizması sağlayan decorator.

    Args:
        max_attempts (int): Maksimum deneme sayısı.
        delay_seconds (int): Denemeler arası bekleme süresi (saniye).

    Returns:
        callable: Decorator fonksiyonu.
    """

    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            attempts = 0
            last_exception = None

            while attempts < max_attempts:
                try:
                    return func(*args, **kwargs)
                except (ConnectionFailure, ServerSelectionTimeoutError) as e:
                    attempts += 1
                    last_exception = e
                    logger.warning(f"MongoDB bağlantı hatası: {str(e)}. "
                                   f"Deneme {attempts}/{max_attempts}...")

                    if attempts < max_attempts:
                        logger.info(f"{delay_seconds} saniye bekleniyor...")
                        time.sleep(delay_seconds)

            logger.error(f"Maksimum deneme sayısına ulaşıldı. Son hata: {str(last_exception)}")
            raise last_exception

        return wrapper

    return decorator


class DatabaseHandler:
    """
    MongoDB işlemlerini yöneten sınıf.
    """

    def __init__(
            self,
            connection_string=config.MONGO_CONNECTION_STRING,
            database_name=config.MONGO_DATABASE
    ):
        """
        Args:
            connection_string (str): MongoDB bağlantı URL'si.
            database_name (str): Kullanılacak veritabanının adı.
        """
        self.connection_string = connection_string
        self.database_name = database_name
        self.client = None
        self.db = None

    def connect(self):
        """
        MongoDB'ye bağlanır.

        Returns:
            pymongo.database.Database: Veritabanı bağlantısı.
        """
        if self.client is None:
            logger.info(f"MongoDB'ye bağlanılıyor: {self.database_name}")
            self.client = MongoClient(self.connection_string)
            self.db = self.client[self.database_name]

            # Bağlantıyı test et
            try:
                self.client.admin.command('ping')
                logger.info("MongoDB bağlantısı başarılı.")
            except Exception as e:
                logger.error(f"MongoDB bağlantı testi başarısız: {str(e)}")
                raise

        return self.db

    def close(self):
        """
        MongoDB bağlantısını kapatır.
        """
        if self.client:
            logger.info("MongoDB bağlantısı kapatılıyor.")
            self.client.close()
            self.client = None
            self.db = None

    @retry_on_mongodb_error()
    @log_function_call
    def insert_gold_prices(self, prices_data):
        """
        Altın fiyatlarını veritabanına kaydeder.

        Args:
            prices_data (dict): Altın fiyat verileri.

        Returns:
            pymongo.results.InsertOneResult: Ekleme sonucu.
        """
        db = self.connect()
        collection = db[config.MONGO_COLLECTION_PRICES]

        # Bugünün tarihini al (saat bilgisi olmadan)
        current_date = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
        today = current_date.strftime(config.DATE_FORMAT)

        # Aynı güne ait veri var mı kontrol et
        existing_record = collection.find_one({'date': today})
        if existing_record:
            logger.warning(f"'{today}' tarihine ait veri zaten mevcut. Güncelleniyor...")
            result = collection.replace_one({'date': today}, {'date': today, 'data': prices_data})
            logger.info(f"Veri güncellendi. Etkilenen kayıt sayısı: {result.modified_count}")
            return result
        else:
            # Yeni kayıt ekle
            data_with_date = {'date': today, 'data': prices_data}
            result = collection.insert_one(data_with_date)
            logger.info(f"Veri kaydedildi. Eklenen kayıt ID: {result.inserted_id}")
            return result

    @retry_on_mongodb_error()
    @log_function_call
    def insert_percentage_differences(self, date, percentage_diff):
        """
        Yüzde değişim verilerini veritabanına kaydeder.

        Args:
            date (str): Veri tarihi.
            percentage_diff (dict): Yüzde değişim verileri.

        Returns:
            pymongo.results.InsertOneResult: Ekleme sonucu.
        """
        db = self.connect()
        collection = db[config.MONGO_COLLECTION_PERCENTAGES]

        # Aynı güne ait veri var mı kontrol et
        existing_record = collection.find_one({'date': date})
        if existing_record:
            logger.warning(f"'{date}' tarihine ait yüzde değişim verisi zaten mevcut. Güncelleniyor...")
            result = collection.replace_one(
                {'date': date},
                {'date': date, 'percentage_difference': percentage_diff}
            )
            logger.info(f"Yüzde değişim verisi güncellendi. Etkilenen kayıt sayısı: {result.modified_count}")
            return result
        else:
            # Yeni kayıt ekle
            document = {
                'date': date,
                'percentage_difference': percentage_diff
            }
            result = collection.insert_one(document)
            logger.info(f"Yüzde değişim verisi kaydedildi. Eklenen kayıt ID: {result.inserted_id}")
            return result

    @retry_on_mongodb_error()
    @log_function_call
    def get_latest_gold_prices(self):
        """
        En son altın fiyat verilerini getirir.

        Returns:
            dict or None: En son altın fiyat kaydı.
        """
        db = self.connect()
        collection = db[config.MONGO_COLLECTION_PRICES]

        latest_document = collection.find_one(sort=[('date', -1)])
        if latest_document:
            logger.info(f"En son fiyat verisi bulundu: {latest_document['date']}")
        else:
            logger.warning("Hiç fiyat verisi bulunamadı.")

        return latest_document

    @retry_on_mongodb_error()
    @log_function_call
    def get_gold_prices_by_date(self, date):
        """
        Belirli bir tarihe ait altın fiyat verilerini getirir.

        Args:
            date (str): İstenen tarih (YYYY-MM-DD formatında).

        Returns:
            dict or None: İstenen tarihe ait altın fiyat kaydı.
        """
        db = self.connect()
        collection = db[config.MONGO_COLLECTION_PRICES]

        document = collection.find_one({'date': date})
        if document:
            logger.info(f"'{date}' tarihine ait veri bulundu.")
        else:
            logger.warning(f"'{date}' tarihine ait veri bulunamadı.")

        return document