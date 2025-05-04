# Gold Price Manager

Altın fiyatlarını çeşitli kaynaklardan toplayan, işleyen ve MongoDB'de saklayan modüler bir sistem.

## Özellikler

- **Modüler Yapı**: Kolayca yeni veri kaynakları eklenebilir
- **Çoklu Veri Kaynağı Desteği**: Birden fazla kaynaktan veri çekme
- **Veri Doğrulama**: Toplanan verilerin doğruluğunu ve tutarlılığını kontrol eder
- **Otomatik Yüzde Hesaplaması**: Günlük fiyat değişimlerini hesaplar
- **Gelişmiş Hata Yönetimi**: Hataları yakalar ve uygun şekilde işler
- **Detaylı Loglama**: Tüm işlemleri ayrıntılı şekilde loglar
- **Veritabanı Önbelleği**: Verileri tekrar kullanım için saklar
- **Çalışma Modu Seçenekleri**: Farklı işlem modları (toplama, hesaplama, tam döngü)
- **Periyodik Çalışma**: Belirli aralıklarla otomatik çalışma

## Kurulum

1. Python 3.8+ yüklü olmalıdır.
2. Gerekli paketleri yükleyin:

```bash
pip install -r requirements.txt
```

3. `.env` dosyasını düzenleyin (örnek için bkz. `.env.example`).
4. MongoDB'yi başlatın:

```bash
mongod --dbpath /path/to/db
```

## Kullanım

### Komut Satırı Parametreleri

```bash
python main.py [seçenekler]
```

Seçenekler:
- `--collect`: Sadece altın fiyatlarını toplar
- `--calculate`: Sadece yüzde değişim hesaplar
- `--full`: Tam döngü çalıştırır (varsayılan)
- `--interval N`: Kontrol aralığını N dakika olarak ayarlar
- `--loops N`: Çalıştırılacak döngü sayısını belirler (0: sınırsız)

Örnekler:

```bash
# Tam döngü 1 kez çalıştır (varsayılan)
python main.py

# Sadece veri topla
python main.py --collect

# Sadece yüzde hesapla
python main.py --calculate

# Tam döngüyü 30 dakika aralıklarla sınırsız çalıştır
python main.py --full --interval 30 --loops 0
```

### Cron Job Olarak Çalıştırma

Sistemi günlük olarak çalıştırmak için crontab'a aşağıdaki satırı ekleyin:

```
0 9 * * * cd /path/to/gold_price_manager && python main.py >> logs/cron.log 2>&1
```

## Proje Yapısı

```
gold_price_manager/
  ├── .env                   # Çevresel değişkenler
  ├── main.py                # Ana uygulama modülü
  ├── config.py              # Konfigürasyon modülü
  ├── utils/                 # Yardımcı modüller
  │   ├── __init__.py
  │   ├── logger.py          # Loglama modülü
  │   ├── db_handler.py      # Veritabanı işlemleri
  │   └── validators.py      # Veri doğrulama
  ├── data_sources/          # Veri kaynakları
  │   ├── __init__.py
  │   ├── base_source.py     # Temel veri kaynağı sınıfı
  │   ├── uzmanpara_source.py # UzmanPara sitesi adaptörü
  │   └── interface.py       # Veri kaynakları arayüzü
  ├── processors/            # Veri işleyicileri
  │   ├── __init__.py
  │   ├── percentage_calculator.py # Yüzde hesaplama
  │   └── data_validator.py  # Veri doğrulama
  ├── models/                # Veri modelleri
  │   ├── __init__.py
  │   ├── gold_price.py      # Altın fiyat modeli
  │   └── percentage_model.py # Yüzde değişim modeli
  └── README.md              # Bu dosya
```

## Yeni Veri Kaynağı Ekleme

Yeni bir veri kaynağı eklemek için aşağıdaki adımları izleyin:

1. `data_sources` klasörüne yeni bir Python dosyası ekleyin (ör. `my_source.py`).
2. `BaseDataSource` sınıfından türetilen bir sınıf oluşturun:

```python
from data_sources.base_source import BaseDataSource

class MySource(BaseDataSource):
    def __init__(self):
        super().__init__(
            name="MySource",
            url="https://example.com/gold-prices"
        )
    
    def fetch_data(self):
        # Veri çekme işlemini uygulayın
        response = self.get_http_response()
        return response
    
    def process_data(self, response):
        # Ham veriyi işleyin ve standart formata dönüştürün
        prices_data = {}
        # ... veri işleme ...
        return prices_data
```

3. `config.py` dosyasındaki `ACTIVE_SOURCES` listesine ekleyin:

```python
ACTIVE_SOURCES = ['UzmanParaSource', 'MySource']
```

## Veri Formatı

### MongoDB'de Saklanan Veri Yapısı

#### Altın Fiyatları Koleksiyonu (`prices`)

```json
{
  "_id": ObjectId("..."),
  "date": "2025-04-05",
  "data": {
    "Gram Altın": {
      "Alış Fiyatı": 3700,
      "Satış Fiyatı": 3700
    },
    "Çeyrek Altın": {
      "Alış Fiyatı": 6266,
      "Satış Fiyatı": 6204
    },
    ...
  }
}
```

#### Yüzde Değişim Koleksiyonu (`daily_percentage`)

```json
{
  "_id": ObjectId("..."),
  "date": "2025-04-06",
  "percentage_difference": {
    "Gram Altın": {
      "Alış Fiyatı": 0.5,
      "Satış Fiyatı": 0.3
    },
    "Çeyrek Altın": {
      "Alış Fiyatı": -0.2,
      "Satış Fiyatı": -0.1
    },
    ...
  }
}
```

## Hata Ayıklama

### Loglama

Loglar, kodun çeşitli noktalarında bilgi, uyarı ve hata mesajları içerir. Konsola ve log dosyasına yazılır.

### Uyarılar ve Hatalar

Sistem çalışırken oluşabilecek olası uyarı ve hatalar:

- Veri kaynağına erişim hataları
- Veri ayrıştırma hataları
- Veritabanı bağlantı hataları
- Veri doğrulama hataları
- Karşılaştırma için gerekli belgelerin bulunamama durumu

### İzleme ve Bakım

- Log dosyasını düzenli olarak kontrol edin
- MongoDB veritabanını ve koleksiyonları düzenli olarak yedekleyin
- Sistemin düzgün çalıştığından emin olmak için periyodik kontroller yapın

## Lisans

MIT Lisansı

## İletişim

Proje ile ilgili sorularınız için: example@example.com