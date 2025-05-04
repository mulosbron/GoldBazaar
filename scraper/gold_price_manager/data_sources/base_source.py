"""
Veri kaynakları için temel sınıf - Tüm veri kaynakları bu sınıfı genişletmelidir.
"""
from abc import ABC, abstractmethod
import time
import requests
from requests.exceptions import RequestException

import config
from utils.logger import setup_logger, log_function_call

logger = setup_logger(__name__)


class BaseDataSource(ABC):
    """
    Veri kaynakları için soyut temel sınıf.
    """

    def __init__(self, name, url=None, headers=None):
        """
        Args:
            name (str): Veri kaynağının adı.
            url (str, optional): Veri kaynağının URL'si.
            headers (dict, optional): HTTP istek başlıkları.
        """
        self.name = name
        self.url = url
        self.headers = headers or config.HEADERS

    @abstractmethod
    def fetch_data(self):
        """
        Kaynaktan veri çeker.

        Returns:
            dict: Çekilen veriler.

        Raises:
            NotImplementedError: Bu metod alt sınıflar tarafından uygulanmalıdır.
        """
        pass

    @abstractmethod
    def process_data(self, raw_data):
        """
        Ham verileri işler.

        Args:
            raw_data: Kaynaktan çekilen ham veriler.

        Returns:
            dict: İşlenmiş veriler.

        Raises:
            NotImplementedError: Bu metod alt sınıflar tarafından uygulanmalıdır.
        """
        pass

    def retry_fetch(self, max_attempts=config.RETRY_ATTEMPTS, delay_seconds=config.RETRY_DELAY_SECONDS):
        """
        Hata durumunda yeniden deneme mekanizması ile veri çeker.

        Args:
            max_attempts (int): Maksimum deneme sayısı.
            delay_seconds (int): Denemeler arası bekleme süresi (saniye).

        Returns:
            dict: Çekilen veriler.

        Raises:
            RequestException: Tüm denemeler başarısız olursa.
        """
        attempts = 0
        last_exception = None

        while attempts < max_attempts:
            try:
                logger.info(f"[{self.name}] Veriler çekiliyor ({attempts + 1}/{max_attempts})...")
                return self.fetch_data()
            except RequestException as e:
                attempts += 1
                last_exception = e
                logger.warning(f"[{self.name}] Veri çekme hatası: {str(e)}. "
                               f"Deneme {attempts}/{max_attempts}...")

                if attempts < max_attempts:
                    logger.info(f"[{self.name}] {delay_seconds} saniye bekleniyor...")
                    time.sleep(delay_seconds)

        logger.error(f"[{self.name}] Maksimum deneme sayısına ulaşıldı. Son hata: {str(last_exception)}")
        raise last_exception

    @log_function_call
    def get_http_response(self, url=None, method='GET', **kwargs):
        """
        HTTP isteği gönderir ve yanıtı döndürür.

        Args:
            url (str, optional): İstek URL'si. Belirtilmezse self.url kullanılır.
            method (str): HTTP metodu ('GET', 'POST', vs.).
            **kwargs: requests.request metodu için ek parametreler.

        Returns:
            requests.Response: HTTP yanıtı.

        Raises:
            RequestException: HTTP isteği başarısız olursa.
        """
        request_url = url or self.url
        if not request_url:
            raise ValueError("URL belirtilmedi.")

        logger.debug(f"[{self.name}] HTTP {method} isteği: {request_url}")

        # Headers sağlanmamışsa varsayılan headers kullan
        if 'headers' not in kwargs:
            kwargs['headers'] = self.headers

        # HTTP isteği gönder
        response = requests.request(method, request_url, **kwargs)

        # Durum kodunu kontrol et
        response.raise_for_status()

        logger.debug(f"[{self.name}] HTTP isteği başarılı: {response.status_code}")
        return response

    @log_function_call
    def get_data(self):
        """
        Veri kaynağından veri çeker ve işler.

        Returns:
            dict: İşlenmiş veri.
        """
        try:
            raw_data = self.retry_fetch()
            processed_data = self.process_data(raw_data)
            logger.info(f"[{self.name}] Veriler başarıyla çekildi ve işlendi.")
            return processed_data
        except Exception as e:
            logger.error(f"[{self.name}] Veri çekme ve işleme hatası: {str(e)}")
            raise