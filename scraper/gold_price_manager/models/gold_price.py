"""
Altın fiyat modeli - Veritabanındaki altın fiyat verilerini temsil eder.
"""
from datetime import datetime
from typing import Dict, Any, Optional


class GoldPriceDetail:
    """
    Bir altın türü için alış ve satış fiyatlarını tutan sınıf.
    """

    def __init__(self, buying_price: float, selling_price: float):
        """
        Args:
            buying_price (float): Alış fiyatı.
            selling_price (float): Satış fiyatı.
        """
        self.buying_price = buying_price
        self.selling_price = selling_price

    @classmethod
    def from_dict(cls, data: Dict[str, float]) -> 'GoldPriceDetail':
        """
        Sözlükten GoldPriceDetail nesnesi oluşturur.

        Args:
            data (dict): Alış ve satış fiyatlarını içeren sözlük.
                Örnek: {'Alış Fiyatı': 1000.0, 'Satış Fiyatı': 1010.0}

        Returns:
            GoldPriceDetail: Oluşturulan nesne.
        """
        return cls(
            buying_price=data.get('Alış Fiyatı', 0.0),
            selling_price=data.get('Satış Fiyatı', 0.0)
        )

    def to_dict(self) -> Dict[str, float]:
        """
        Nesneyi sözlüğe dönüştürür.

        Returns:
            dict: Alış ve satış fiyatlarını içeren sözlük.
        """
        return {
            'Alış Fiyatı': self.buying_price,
            'Satış Fiyatı': self.selling_price
        }

    def __str__(self) -> str:
        """
        Nesneyi string'e dönüştürür.

        Returns:
            str: Nesne string temsili.
        """
        return f"Alış: {self.buying_price:.2f}, Satış: {self.selling_price:.2f}"


class GoldPrice:
    """
    Belirli bir tarihteki tüm altın türlerinin fiyat bilgilerini tutan sınıf.
    """

    def __init__(
            self,
            date: str,
            gold_data: Dict[str, Dict[str, float]],
            id: Optional[str] = None
    ):
        """
        Args:
            date (str): Fiyat tarihi (YYYY-MM-DD formatında).
            gold_data (dict): Altın türlerinin fiyat bilgilerini içeren sözlük.
                Örnek: {'Gram Altın': {'Alış Fiyatı': 1000.0, 'Satış Fiyatı': 1010.0}, ...}
            id (str, optional): MongoDB belge ID'si.
        """
        self.id = id
        self.date = date
        self.data = {}

        # Altın verilerini işle
        for gold_type, price_info in gold_data.items():
            self.data[gold_type] = GoldPriceDetail.from_dict(price_info)

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'GoldPrice':
        """
        MongoDB belgesinden GoldPrice nesnesi oluşturur.

        Args:
            data (dict): MongoDB belgesi.

        Returns:
            GoldPrice: Oluşturulan nesne.
        """
        return cls(
            id=str(data.get('_id', '')),
            date=data.get('date', ''),
            gold_data=data.get('data', {})
        )

    def to_dict(self) -> Dict[str, Any]:
        """
        Nesneyi MongoDB belgesine dönüştürür.

        Returns:
            dict: MongoDB belgesi.
        """
        data_dict = {}
        for gold_type, price_detail in self.data.items():
            data_dict[gold_type] = price_detail.to_dict()

        result = {
            'date': self.date,
            'data': data_dict
        }

        return result

    def get_price(self, gold_type: str) -> Optional[GoldPriceDetail]:
        """
        Belirli bir altın türünün fiyat bilgisini döndürür.

        Args:
            gold_type (str): Altın türü.

        Returns:
            GoldPriceDetail or None: Fiyat bilgisi veya bulunamazsa None.
        """
        return self.data.get(gold_type)

    def __str__(self) -> str:
        """
        Nesneyi string'e dönüştürür.

        Returns:
            str: Nesne string temsili.
        """
        return f"GoldPrice(date={self.date}, gold_types={len(self.data)})"