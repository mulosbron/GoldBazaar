"""
Yapılandırma modülü - Çevresel değişkenleri ve uygulama yapılandırmasını yönetir.
"""
import os
from pathlib import Path
from dotenv import load_dotenv

# .env dosyasını yükle
dotenv_path = Path(__file__).parent / '.env'
load_dotenv(dotenv_path=dotenv_path)

# MongoDB Yapılandırması
MONGO_CONNECTION_STRING = os.getenv('MONGO_CONNECTION_STRING', 'mongodb://localhost:27017/')
MONGO_DATABASE = os.getenv('MONGO_DATABASE', 'gold_prices')
MONGO_COLLECTION_PRICES = os.getenv('MONGO_COLLECTION_PRICES', 'prices')
MONGO_COLLECTION_PERCENTAGES = os.getenv('MONGO_COLLECTION_PERCENTAGES', 'daily_percentage')

# Veri Kaynakları
UZMANPARA_URL = os.getenv('UZMANPARA_URL', 'https://uzmanpara.milliyet.com.tr/altin-fiyatlari/')

# HTTP İstek Ayarları
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                  "AppleWebKit/537.36 (KHTML, like Gecko) "
                  "Chrome/118.0.0.0 Safari/537.36"
}

# Loglama Ayarları
LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')
LOG_FILE = os.getenv('LOG_FILE', 'gold_price_manager.log')

# Uygulama Ayarları
CHECK_INTERVAL_MINUTES = int(os.getenv('CHECK_INTERVAL_MINUTES', 1440))  # Varsayılan: 24 saat
RETRY_ATTEMPTS = int(os.getenv('RETRY_ATTEMPTS', 3))
RETRY_DELAY_SECONDS = int(os.getenv('RETRY_DELAY_SECONDS', 60))

# Tarih formatı
DATE_FORMAT = '%Y-%m-%d'
DATETIME_FORMAT = '%Y-%m-%d %H:%M:%S'

# Aktif Veri Kaynakları (Sonradan eklenecek kaynakları buraya dahil edebilirsiniz)
ACTIVE_SOURCES = ['UzmanParaSource']