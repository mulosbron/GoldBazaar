"""
Loglama modülü - Uygulama loglarını yönetir.
"""
import logging
import sys
from datetime import datetime
from logging.handlers import RotatingFileHandler

import config

# Logger formatı
LOG_FORMAT = '%(asctime)s [%(levelname)s] %(name)s: %(message)s'


def setup_logger(name=None, level=None, log_file=None):
    """
    Belirtilen yapılandırmayla bir logger oluşturur.

    Args:
        name (str, optional): Logger adı. Varsayılan: None (kök logger).
        level (str, optional): Log seviyesi. Varsayılan: config.LOG_LEVEL.
        log_file (str, optional): Log dosyası yolu. Varsayılan: config.LOG_FILE.

    Returns:
        logging.Logger: Yapılandırılmış logger nesnesi.
    """
    if level is None:
        level = getattr(logging, config.LOG_LEVEL)

    if log_file is None:
        log_file = config.LOG_FILE

    # Logger oluştur
    logger = logging.getLogger(name)
    logger.setLevel(level)

    # Aynı handler'ları tekrar eklememek için temizle
    if logger.handlers:
        logger.handlers.clear()

    # Formatter oluştur
    formatter = logging.Formatter(LOG_FORMAT, datefmt=config.DATETIME_FORMAT)

    # Console handler
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)

    # Dosya handler (rotasyonlu)
    file_handler = RotatingFileHandler(
        log_file, maxBytes=10 * 1024 * 1024, backupCount=5
    )
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)

    return logger


def log_function_call(func):
    """
    Fonksiyon çağrılarını loglayan decorator.

    Args:
        func (callable): Loglanacak fonksiyon.

    Returns:
        callable: Decorator wrapper fonksiyonu.
    """
    logger = setup_logger(func.__module__)

    def wrapper(*args, **kwargs):
        logger.debug(f"Çağrılan fonksiyon: {func.__name__}")
        try:
            result = func(*args, **kwargs)
            logger.debug(f"Fonksiyon tamamlandı: {func.__name__}")
            return result
        except Exception as e:
            logger.error(f"Fonksiyon başarısız: {func.__name__}, Hata: {str(e)}")
            raise

    return wrapper


# Önceki koddaki custom_print fonksiyonunu logger ile değiştiriyoruz
def custom_print(*args, **kwargs):
    """
    Eski custom_print fonksiyonunu taklit eden logger wrapper'ı.

    Args:
        *args: Loglanacak bileşenler.
        **kwargs: Ek parametreler.
    """
    logger = setup_logger("custom_print")
    message = " ".join(str(arg) for arg in args)
    logger.info(message)


# Uygulama çapında kullanılacak ana logger
app_logger = setup_logger("gold_price_manager")