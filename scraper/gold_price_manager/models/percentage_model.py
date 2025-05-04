"""
Yüzde değişim modeli - Veritabanındaki yüzde değişim verilerini temsil eder.
"""
from datetime import datetime
from typing import Dict, Any, Optional, List, Set


class PercentageDetail:
    """
    Bir altın türü için alış ve satış fiyatlarının yüzde değişimlerini tutan sınıf.
    """

    def __init__(self, buying_percentage: float, selling_percentage: float):
        """
        Args:
            buying_percentage (float): Alış fiyatının yüzde değişimi.
            selling_percentage (float): Satış fiyatının yüzde değişimi.
        """
        self.buying_percentage = buying_percentage
        self.selling_percentage = selling_percentage

    @classmethod
    def from_dict(cls, data: Dict[str, float]) -> 'PercentageDetail':
        """
        Sözlükten PercentageDetail nesnesi oluşturur.

        Args:
            data (dict): Alış ve satış yüzde değişimlerini içeren sözlük.
                Örnek: {'Alış Fiyatı': 2.5, 'Satış Fiyatı': 1.8}

        Returns:
            PercentageDetail: Oluşturulan nesne.
        """
        return cls(
            buying_percentage=data.get('Alış Fiyatı', 0.0),
            selling_percentage=data.get('Satış Fiyatı', 0.0)
        )

    def to_dict(self) -> Dict[str, float]:
        """
        Nesneyi sözlüğe dönüştürür.

        Returns:
            dict: Alış ve satış yüzde değişimlerini içeren sözlük.
        """
        return {
            'Alış Fiyatı': self.buying_percentage,
            'Satış Fiyatı': self.selling_percentage
        }

    def __str__(self) -> str:
        """
        Nesneyi string'e dönüştürür.

        Returns:
            str: Nesne string temsili.
        """
        return f"Alış: %{self.buying_percentage:.2f}, Satış: %{self.selling_percentage:.2f}"


class PercentageDifference:
    """
    Belirli bir tarihteki tüm altın türlerinin yüzde değişim bilgilerini tutan sınıf.
    """

    def __init__(
            self,
            date: str,
            percentage_data: Dict[str, Dict[str, float]],
            id: Optional[str] = None
    ):
        """
        Args:
            date (str): Fiyat tarihi (YYYY-MM-DD formatında).
            percentage_data (dict): Altın türlerinin yüzde değişim bilgilerini içeren sözlük.
                Örnek: {'Gram Altın': {'Alış Fiyatı': 2.5, 'Satış Fiyatı': 1.8}, ...}
            id (str, optional): MongoDB belge ID'si.
        """
        self.id = id
        self.date = date
        self.percentage_difference = {}

        # Yüzde değişim verilerini işle
        for gold_type, percentage_info in percentage_data.items():
            self.percentage_difference[gold_type] = PercentageDetail.from_dict(percentage_info)

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'PercentageDifference':
        """
        MongoDB belgesinden PercentageDifference nesnesi oluşturur.

        Args:
            data (dict): MongoDB belgesi.

        Returns:
            PercentageDifference: Oluşturulan nesne.
        """
        return cls(
            id=str(data.get('_id', '')),
            date=data.get('date', ''),
            percentage_data=data.get('percentage_difference', {})
        )

    def to_dict(self) -> Dict[str, Any]:
        """
        Nesneyi MongoDB belgesine dönüştürür.

        Returns:
            dict: MongoDB belgesi.
        """
        percentage_dict = {}
        for gold_type, percentage_detail in self.percentage_difference.items():
            percentage_dict[gold_type] = percentage_detail.to_dict()

        result = {
            'date': self.date,
            'percentage_difference': percentage_dict
        }

        return result

    def get_top_increases(self, limit: int = 5) -> List[Dict[str, Any]]:
        """
        Alış fiyatında en yüksek artış gösteren altın türlerini döndürür.

        Args:
            limit (int): Döndürülecek maksimum kayıt sayısı.

        Returns:
            list: Altın türlerinin artış bilgilerini içeren liste.
        """
        increases = []

        for gold_type, detail in self.percentage_difference.items():
            if detail.buying_percentage > 0:
                increases.append({
                    'gold_type': gold_type,
                    'percentage': detail.buying_percentage
                })

        # Yüzde değişimine göre azalan sırada sırala
        increases.sort(key=lambda x: x['percentage'], reverse=True)

        return increases[:limit]

    def get_top_decreases(self, limit: int = 5) -> List[Dict[str, Any]]:
        """
        Alış fiyatında en yüksek düşüş gösteren altın türlerini döndürür.

        Args:
            limit (int): Döndürülecek maksimum kayıt sayısı.

        Returns:
            list: Altın türlerinin düşüş bilgilerini içeren liste.
        """
        decreases = []

        for gold_type, detail in self.percentage_difference.items():
            if detail.buying_percentage < 0:
                decreases.append({
                    'gold_type': gold_type,
                    'percentage': detail.buying_percentage
                })

        # Yüzde değişimine göre artan sırada sırala (en düşük değerler önce)
        decreases.sort(key=lambda x: x['percentage'])

        return decreases[:limit]

    def get_all_gold_types(self) -> Set[str]:
        """
        Tüm altın türlerinin listesini döndürür.

        Returns:
            set: Altın türlerinin kümesi.
        """
        return set(self.percentage_difference.keys())

    def __str__(self) -> str:
        """
        Nesneyi string'e dönüştürür.

        Returns:
            str: Nesne string temsili.
        """
        return f"PercentageDifference(date={self.date}, gold_types={len(self.percentage_difference)})"