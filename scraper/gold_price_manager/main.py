"""
Ana uygulama modülü - Altın fiyat verilerini toplama, işleme ve veritabanına kaydetme işlemlerini koordine eder.
"""
import sys
import time
import argparse
from datetime import datetime

import config
from utils.logger import setup_logger, log_function_call
from utils.db_handler import DatabaseHandler
from data_sources.interface import get_all_active_sources
from processors.data_validator import DataValidator
from processors.percentage_calculator import PercentageCalculator

logger = setup_logger(__name__)


@log_function_call
def fetch_gold_prices():
    """
    Tüm aktif kaynaklardan altın fiyatlarını toplar.

    Returns:
        dict: Toplanan altın fiyat verileri.
    """
    logger.info("Tüm aktif veri kaynaklarından altın fiyatları çekiliyor...")

    # Tüm aktif veri kaynaklarını al
    data_sources = get_all_active_sources()
    if not data_sources:
        logger.error("Aktif veri kaynağı bulunamadı!")
        return None

    # Her kaynaktan veri çek ve birleştir
    all_prices = {}
    for source in data_sources:
        try:
            logger.info(f"'{source.name}' kaynağından veri çekiliyor...")
            source_prices = source.get_data()

            # Veri kontrolü
            if not source_prices:
                logger.warning(f"'{source.name}' kaynağından veri alınamadı.")
                continue

            logger.info(f"'{source.name}' kaynağından {len(source_prices)} altın türü fiyatı alındı.")

            # Farklı kaynaklardan farklı altın türleri olabilir, birleştir
            all_prices.update(source_prices)

        except Exception as e:
            logger.error(f"'{source.name}' kaynağından veri çekme hatası: {str(e)}")

    if not all_prices:
        logger.error("Hiçbir kaynaktan veri alınamadı!")
        return None

    logger.info(f"Toplam {len(all_prices)} farklı altın türü için fiyat verisi çekildi.")
    return all_prices


@log_function_call
def run_gold_price_collection():
    """
    Altın fiyat toplama işlemini çalıştırır.

    Returns:
        bool: İşlem başarılıysa True, değilse False.
    """
    logger.info("Altın fiyat toplama işlemi başlatılıyor...")

    try:
        # Veritabanı bağlantısı oluştur
        db_handler = DatabaseHandler()

        # Veri topla
        prices = fetch_gold_prices()
        if not prices:
            logger.error("Fiyat verisi toplanamadı. İşlem sonlandırılıyor.")
            return False

        # Veri doğrula
        validator = DataValidator(db_handler)
        if not validator.validate_complete_dataset(prices):
            logger.error("Veri doğrulama başarısız. İşlem sonlandırılıyor.")
            return False

        # Veritabanına kaydet
        db_handler.insert_gold_prices(prices)
        logger.info("Altın fiyatları başarıyla veritabanına kaydedildi.")

        # Bağlantıyı kapat
        db_handler.close()

        return True

    except Exception as e:
        logger.error(f"Altın fiyat toplama işlemi sırasında hata: {str(e)}")
        return False


@log_function_call
def run_percentage_calculation():
    """
    Yüzde değişim hesaplama işlemini çalıştırır.

    Returns:
        bool: İşlem başarılıysa True, değilse False.
    """
    logger.info("Yüzde değişim hesaplama işlemi başlatılıyor...")

    try:
        # Veritabanı bağlantısı oluştur
        db_handler = DatabaseHandler()

        # Yüzde değişimi hesapla
        calculator = PercentageCalculator(db_handler)
        result = calculator.process_daily_differences()

        # Bağlantıyı kapat
        db_handler.close()

        if result is not None:
            logger.info("Yüzde değişim hesaplama işlemi başarıyla tamamlandı.")
            return True
        else:
            logger.warning("Yüzde değişim hesaplama işlemi sonuç üretmedi.")
            return False

    except Exception as e:
        logger.error(f"Yüzde değişim hesaplama işlemi sırasında hata: {str(e)}")
        return False


@log_function_call
def run_full_process():
    """
    Tam işlem döngüsünü çalıştırır: veri toplama ve yüzde hesaplama.

    Returns:
        bool: İşlem başarılıysa True, değilse False.
    """
    logger.info("Tam altın fiyat işleme döngüsü başlatılıyor...")

    # Fiyatları topla
    price_result = run_gold_price_collection()
    if not price_result:
        logger.error("Fiyat toplama işlemi başarısız olduğundan tam döngü tamamlanamadı.")
        return False

    # Biraz bekle (işlemlerin veritabanına tamamen yazılması için)
    time.sleep(2)

    # Yüzde değişimleri hesapla
    percentage_result = run_percentage_calculation()
    if not percentage_result:
        logger.warning("Yüzde değişim hesaplama başarısız oldu, ancak fiyat toplama işlemi başarılıydı.")
        return False

    logger.info("Tam işlem döngüsü başarıyla tamamlandı.")
    return True


def setup_arguments():
    """
    Komut satırı argümanlarını ayarlar.

    Returns:
        argparse.Namespace: Ayrıştırılmış argümanlar.
    """
    parser = argparse.ArgumentParser(description='Altın Fiyat Yöneticisi')

    # İşlem modu
    mode_group = parser.add_mutually_exclusive_group()
    mode_group.add_argument('--collect', action='store_true',
                            help='Sadece altın fiyatlarını topla')
    mode_group.add_argument('--calculate', action='store_true',
                            help='Sadece yüzde değişim hesapla')
    mode_group.add_argument('--full', action='store_true',
                            help='Tam döngü çalıştır (toplama + hesaplama)')

    # Diğer seçenekler
    parser.add_argument('--interval', type=int,
                        help=f'Kontrol aralığı (dakika), varsayılan: {config.CHECK_INTERVAL_MINUTES}')
    parser.add_argument('--loops', type=int, default=1,
                        help='Çalıştırılacak döngü sayısı, sürekli için 0 girin')

    return parser.parse_args()


def main():
    """
    Ana uygulama girişi.
    """
    # Argümanları ayarla
    args = setup_arguments()

    # Başlangıç logu
    logger.info(f"Altın Fiyat Yönetici v1.0 başlatılıyor. Tarih: {datetime.now().strftime(config.DATETIME_FORMAT)}")

    # Çalıştırma modu
    run_mode = "full"  # Varsayılan mod
    if args.collect:
        run_mode = "collect"
    elif args.calculate:
        run_mode = "calculate"
    elif args.full:
        run_mode = "full"

    # Aralık ve döngü ayarları
    interval_minutes = args.interval or config.CHECK_INTERVAL_MINUTES
    max_loops = args.loops
    current_loop = 0

    logger.info(f"Çalıştırma modu: {run_mode}, "
                f"Kontrol aralığı: {interval_minutes} dakika, "
                f"Maksimum döngü: {'sınırsız' if max_loops <= 0 else max_loops}")

    try:
        # Ana döngü
        while max_loops <= 0 or current_loop < max_loops:
            current_loop += 1
            loop_start_time = datetime.now()

            logger.info(f"Döngü {current_loop}{' / ' + str(max_loops) if max_loops > 0 else ''} başlatılıyor. "
                        f"Saat: {loop_start_time.strftime('%H:%M:%S')}")

            # Seçilen moda göre işlemi çalıştır
            success = False
            if run_mode == "collect":
                success = run_gold_price_collection()
            elif run_mode == "calculate":
                success = run_percentage_calculation()
            elif run_mode == "full":
                success = run_full_process()

            # Sonuç durumunu logla
            if success:
                logger.info(f"Döngü {current_loop} başarıyla tamamlandı.")
            else:
                logger.error(f"Döngü {current_loop} başarısız oldu.")

            # Son döngüyse çık
            if max_loops > 0 and current_loop >= max_loops:
                logger.info(f"Maksimum döngü sayısına ({max_loops}) ulaşıldı. Uygulama sonlandırılıyor.")
                break

            # Sonraki döngü için bekle
            if max_loops <= 0 or current_loop < max_loops:
                # Döngü tamamlanma süresini hesapla
                loop_duration = (datetime.now() - loop_start_time).total_seconds()

                # Bir sonraki döngü için bekleme süresi
                wait_seconds = max(0, interval_minutes * 60 - loop_duration)

                if wait_seconds > 0:
                    next_run_time = datetime.now().timestamp() + wait_seconds
                    next_run_time_str = datetime.fromtimestamp(next_run_time).strftime('%H:%M:%S')

                    logger.info(f"Bir sonraki döngü için bekleniyor: {int(wait_seconds / 60)} dakika "
                                f"{int(wait_seconds % 60)} saniye. "
                                f"Sonraki çalışma saati: {next_run_time_str}")

                    time.sleep(wait_seconds)

    except KeyboardInterrupt:
        logger.info("Kullanıcı tarafından durduruldu. Uygulama sonlandırılıyor...")
    except Exception as e:
        logger.error(f"Uygulama çalışırken beklenmeyen hata: {str(e)}", exc_info=True)
        return 1

    logger.info("Uygulama başarıyla sonlandı.")
    return 0


if __name__ == "__main__":
    sys.exit(main())