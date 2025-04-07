import numpy as np
import pandas as pd
from pymongo import MongoClient
from datetime import datetime, timedelta

CONNECTION_STRING = "mongodb://localhost:27017"
DB_NAME = 'gold_prices'
COLLECTION_NAME = 'prices'
PERCENTAGE_DIFF_COLLECTION = 'daily_percentage'
DATE_FORMAT = '%Y-%m-%d %H:%M'


def custom_print(*args, **kwargs):
    current_time = datetime.now().strftime(DATE_FORMAT)
    print(f"[{current_time}]", *args, **kwargs)


def find_latest_and_related_documents():
    custom_print("Searching for the latest and related documents...")
    with MongoClient(CONNECTION_STRING) as client:
        collection = client[DB_NAME][COLLECTION_NAME]
        latest_document = collection.find_one(sort=[('date', -1)])

        if not latest_document:
            custom_print("The latest document added was not found.")
            return None, None

        latest_date = datetime.strptime(latest_document['date'], '%Y-%m-%d')
        related_date = latest_date - timedelta(days=1)
        related_document = collection.find_one({'date': related_date.strftime('%Y-%m-%d')})

        if related_document:
            custom_print("Related document found successfully.")
        else:
            custom_print("Related document not found.")

        return latest_document, related_document


def calculate_percentage_difference(df1, df2):
    custom_print("Calculating percentage differences between DataFrames...")

    def percentage_diff(x1, x2):
        return np.where((x1 != 0) & ~np.isnan(x1) & ~np.isnan(x2), (x2 - x1) / x1 * 100, np.nan)

    return df1.combine(df2, func=percentage_diff)


def save_percentage_difference_to_mongodb(json1, percentage_diff):
    custom_print("Saving percentage differences to MongoDB...")
    with MongoClient(CONNECTION_STRING) as client:
        db = client[DB_NAME]
        collection = db[PERCENTAGE_DIFF_COLLECTION]
        document = {
            'date': json1['date'],
            "percentage_difference": percentage_diff.to_dict()
        }
        collection.insert_one(document)
        custom_print("Percentage differences successfully saved.")


def process_and_clean_data():
    custom_print("Starting data cleaning and comparison process...")
    json1, json2 = find_latest_and_related_documents()
    if not json1 or not json2:
        custom_print("Required documents not found.")
        return

    data1 = pd.DataFrame(json1['data'])
    data2 = pd.DataFrame(json2['data'])
    custom_print("Data cleaned and converted to DataFrames.")

    percentage_diff = calculate_percentage_difference(data1, data2)
    custom_print("Percentage Differences:")
    custom_print(percentage_diff.values)

    save_percentage_difference_to_mongodb(json1, percentage_diff)


if __name__ == "__main__":
    process_and_clean_data()