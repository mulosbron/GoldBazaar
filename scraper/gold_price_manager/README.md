# Gold Price Manager

A modular system that collects, processes, and stores gold prices from various sources in MongoDB.

---

## Features

- **Modular Architecture**: Easily extendable with new data sources  
- **Multi-Source Support**: Fetches data from multiple sources  
- **Data Validation**: Ensures accuracy and consistency of collected data  
- **Automatic Percentage Calculation**: Computes daily price changes  
- **Advanced Error Handling**: Captures and processes errors gracefully  
- **Detailed Logging**: Logs all operations thoroughly  
- **Database Caching**: Stores data for reuse  
- **Operation Modes**: Collection, calculation, or full cycle  
- **Periodic Execution**: Can be scheduled to run at set intervals  

---

## Installation

1. Make sure Python 3.8+ is installed.
2. Install required packages:

```bash
pip install -r requirements.txt
```

3. Edit the `.env` file (see `.env.example` for reference).
4. Start MongoDB:

```bash
mongod --dbpath /path/to/db
```

---

## Usage

### Command-Line Arguments

```bash
python main.py [options]
```

Options:
- `--collect`: Collects gold prices only
- `--calculate`: Calculates percentage change only
- `--full`: Runs full cycle (default)
- `--interval N`: Sets execution interval to N minutes
- `--loops N`: Number of cycles to run (0 for infinite)

Examples:

```bash
# Run full cycle once (default)
python main.py

# Only collect data
python main.py --collect

# Only calculate percentages
python main.py --calculate

# Run full cycle every 30 minutes, indefinitely
python main.py --full --interval 30 --loops 0
```

---

### Run as Cron Job

To run the system daily, add the following line to your crontab:

```
0 9 * * * cd /path/to/gold_price_manager && python main.py >> logs/cron.log 2>&1
```

---

## Project Structure

```
gold_price_manager/
  ├── .env                   # Environment variables
  ├── main.py                # Main application module
  ├── config.py              # Configuration module
  ├── utils/                 # Utility modules
  │   ├── __init__.py
  │   ├── logger.py          # Logging module
  │   ├── db_handler.py      # Database operations
  │   └── validators.py      # Data validation
  ├── data_sources/          # Data source adapters
  │   ├── __init__.py
  │   ├── base_source.py     # Base data source class
  │   ├── uzmanpara_source.py # UzmanPara source adapter
  │   └── interface.py       # Data source interface
  ├── processors/            # Data processors
  │   ├── __init__.py
  │   ├── percentage_calculator.py # Percentage calculations
  │   └── data_validator.py  # Validation logic
  ├── models/                # Data models
  │   ├── __init__.py
  │   ├── gold_price.py      # Gold price model
  │   └── percentage_model.py # Percentage change model
  └── README.md              # This file
```

---

## Adding a New Data Source

To add a new data source:

1. Create a new Python file under the `data_sources` folder (e.g., `my_source.py`)
2. Inherit from the `BaseDataSource` class:

```python
from data_sources.base_source import BaseDataSource

class MySource(BaseDataSource):
    def __init__(self):
        super().__init__(
            name="MySource",
            url="https://example.com/gold-prices"
        )
    
    def fetch_data(self):
        # Implement data fetching logic
        response = self.get_http_response()
        return response
    
    def process_data(self, response):
        # Process raw data and convert it to standard format
        prices_data = {}
        # ... data processing ...
        return prices_data
```

3. Add your source to the `ACTIVE_SOURCES` list in `config.py`:

```python
ACTIVE_SOURCES = ['UzmanParaSource', 'MySource']
```