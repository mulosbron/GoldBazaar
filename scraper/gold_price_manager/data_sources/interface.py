"""
Veri kaynakları arayüz modülü - Tüm veri kaynaklarına tek bir noktadan erişim sağlar.
"""
import importlib
import inspect

import config
from data_sources.base_source import BaseDataSource
from utils.logger import setup_logger

logger = setup_logger(__name__)


def get_data_source(source_name):
    """
    İsme göre veri kaynağı oluşturur.

    Args:
        source_name (str): Veri kaynağı sınıfının adı.

    Returns:
        BaseDataSource: Veri kaynağı örneği.

    Raises:
        ImportError: Belirtilen modül bulunamazsa.
        AttributeError: Belirtilen sınıf bulunamazsa.
        TypeError: Sınıf BaseDataSource'dan türetilmemişse.
    """
    try:
        # Modül adı (snake_case dönüşümü)
        module_name = source_name.replace("Source", "").lower() + "_source"
        module_path = f"data_sources.{module_name}"

        # Modülü dinamik olarak içe aktar
        module = importlib.import_module(module_path)

        # Sınıfı bul
        for name, obj in inspect.getmembers(module, inspect.isclass):
            if name == source_name and issubclass(obj, BaseDataSource) and obj != BaseDataSource:
                # Sınıfın bir örneğini oluştur ve döndür
                logger.info(f"Veri kaynağı sınıfı oluşturuluyor: {source_name}")
                return obj()

        raise AttributeError(f"'{module_path}' modülünde '{source_name}' sınıfı bulunamadı")

    except (ImportError, AttributeError, TypeError) as e:
        logger.error(f"Veri kaynağı yüklenirken hata: {str(e)}")
        raise


def get_all_active_sources():
    """
    Aktif veri kaynaklarının listesini döndürür.

    Returns:
        list: Veri kaynağı örneklerinin listesi.
    """
    sources = []
    for source_name in config.ACTIVE_SOURCES:
        try:
            source = get_data_source(source_name)
            sources.append(source)
        except Exception as e:
            logger.error(f"'{source_name}' veri kaynağı yüklenemedi: {str(e)}")

    return sources