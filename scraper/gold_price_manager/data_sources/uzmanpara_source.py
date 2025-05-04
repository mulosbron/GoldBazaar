"""
UzmanPara veri kaynağı - uzmanpara.milliyet.com.tr sitesinden altın fiyatlarını çeker.
"""
from bs4 import BeautifulSoup

import config
from data_sources.base_source import BaseDataSource
from utils.logger import setup_logger, log_function_call

logger = setup_logger(__name__)


class UzmanParaSource(BaseDataSource):
    """
    UzmanPara.milliyet.com.tr sitesinden altın fiyatlarını çeken veri kaynağı.
    """
    def __init__(self):
        """
        UzmanParaSource sınıfını başlatır.
        """
        super().__init__(
            name="UzmanPara",
            url=config.UZMANPARA_URL,
            headers=config.HEADERS
        )

    @log_function_call
    def fetch_data(self):
        """
        UzmanPara sitesinden veri çeker.

        Returns:
            requests.Response: HTTP yanıtı.
        """
        response = self.get_http_response()
        logger.info(f"[{self.name}] Web sayfası başarıyla çekildi: {len(response.content)} byte")
        return response

    @log_function_call
    def process_data(self, response):
        """
        UzmanPara sitesinden çekilen verileri işler.

        Args:
            response (requests.Response): HTTP yanıtı.

        Returns:
            dict: İşlenmiş altın fiyat verileri.
        """
        prices_data = {}

        try:
            # HTML içeriğini ayrıştır
            soup = BeautifulSoup(response.content, 'html.parser')

            # Hedef tabloyu bul (ikinci kutu)
            target_div = soup.find_all('div', class_='box box7 box11')[1]
            rows = target_div.find_all('tr')[1:]  # İlk satır başlık olduğu için atla

            logger.info(f"[{self.name}] Hedef tablo başarıyla ayrıştırıldı: {len(rows)} satır bulundu")

            # Her satırı işle
            for row in rows:
                try:
                    cells = row.find_all('td')

                    # Yeterli hücre var mı kontrol et
                    if len(cells) < 4:
                        logger.warning(f"[{self.name}] Yetersiz hücre sayısı bulundu, satır atlanıyor")
                        continue

                    # Veri ayıklama
                    gold_type = cells[1].text.strip()

                    # TL işaretini kaldır ve nokta/virgül düzeltmesi yap
                    buy_price_text = cells[3].text.strip().replace('.', '').replace(' TL', '').replace(',', '.')
                    sell_price_text = cells[2].text.strip().replace('.', '').replace(' TL', '').replace(',', '.')

                    # Sayısal değere dönüştür
                    try:
                        buy_price = float(buy_price_text)
                        sell_price = float(sell_price_text)
                    except ValueError as e:
                        logger.error(f"[{self.name}] Fiyat değeri dönüştürme hatası: {str(e)}, "
                                    f"Satın alma: '{buy_price_text}', Satış: '{sell_price_text}'")
                        continue

                    # Verileri ekle
                    prices_data[gold_type] = {
                        'Alış Fiyatı': buy_price,
                        'Satış Fiyatı': sell_price,
                    }

                    logger.debug(f"[{self.name}] Fiyat bilgisi çekildi: {gold_type}, "
                                f"Alış: {buy_price}, Satış: {sell_price}")

                except Exception as e:
                    logger.error(f"[{self.name}] Satır işleme hatası: {str(e)}")
                    continue

            logger.info(f"[{self.name}] Toplam {len(prices_data)} farklı altın türü çekildi")

        except Exception as e:
            logger.error(f"[{self.name}] Veri işleme hatası: {str(e)}")
            raise

        return prices_data