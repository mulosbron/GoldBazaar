from pymongo import MongoClient
import requests
from bs4 import BeautifulSoup
from datetime import datetime

URL = 'https://uzmanpara.milliyet.com.tr/altin-fiyatlari/'
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                  "AppleWebKit/537.36 (KHTML, like Gecko) "
                  "Chrome/118.0.0.0 Safari/537.36"
}
MONGO_CONNECTION_STRING = 'mongodb://localhost:27017/'
DATABASE_NAME = 'gold_prices'
COLLECTION_NAME = 'prices'
DATE_FORMAT = '%Y-%m-%d %H:%M'


def custom_print(*args, **kwargs):
    current_time = datetime.now().strftime(DATE_FORMAT)
    print(f"[{current_time}]", *args, **kwargs)


def price_check():
    custom_print("Processing started. Website: " + URL)
    try:
        page = requests.get(URL, headers=HEADERS)
        custom_print("Web page successfully fetched.")
    except Exception as exc:
        custom_print(f"An error occurred while fetching the web page: {exc}")
        return {}

    prices_data = {}

    try:
        soup = BeautifulSoup(page.content, 'html.parser')
        target_div = soup.find_all('div', class_='box box7 box11')[
            1]
        rows = target_div.find_all('tr')[1:]
        custom_print("Target section successfully parsed.")
    except Exception as exc:
        custom_print(f"An error occurred while parsing the target section: {exc}")
        return {}

    for row in rows:
        try:
            cells = row.find_all('td')
            if len(cells) < 4:
                custom_print("Insufficient cells found for the row.")
                continue
            gold_type = cells[1].text.strip()
            buy_price = float(cells[3].text.strip().replace('.', '').replace(' TL', '').replace(',', '.'))
            sell_price = float(cells[2].text.strip().replace('.', '').replace(' TL', '').replace(',', '.'))
            prices_data[gold_type] = {
                'Alış Fiyatı': buy_price,
                'Satış Fiyatı': sell_price,
            }
            custom_print(f"Price information for {gold_type} retrieved: Buy - {buy_price}, Sell - {sell_price}")
        except Exception as exc:
            custom_print(f"An error occurred while retrieving price information for a row: {exc}")

    return prices_data


def save_to_mongo(prices):
    try:
        client = MongoClient(MONGO_CONNECTION_STRING)
        db = client[DATABASE_NAME]
        collection = db[COLLECTION_NAME]
        current_date = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
        today = current_date.strftime('%Y-%m-%d')
        data_with_date = {'date': today, 'data': prices}
        collection.insert_one(data_with_date)
        custom_print("Data successfully saved to MongoDB.")
    except Exception as exc:
        custom_print(f"An error occurred while saving data to MongoDB: {exc}")


if __name__ == '__main__':
    try:
        prices = price_check()
        save_to_mongo(prices)
        custom_print("Process successfully completed. It will be retried after one day.")
    except Exception as exc:
        custom_print(f"An error occurred: {exc}")
