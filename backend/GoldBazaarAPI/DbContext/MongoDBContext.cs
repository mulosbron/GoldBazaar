using MongoDB.Driver;
using GoldBazaarAPI.Models;

namespace GoldBazaarAPI.Data
{
    public class MongoDBContext
    {
        private readonly IMongoDatabase _database;
        private readonly IMongoDatabase _newsDatabase;

        public MongoDBContext(string connectionString, string databaseName)
        {
            var client = new MongoClient(connectionString);
            _database = client.GetDatabase(databaseName);
        }

        // Haber desteği için genişletilmiş constructor
        public MongoDBContext(string connectionString, string databaseName, string newsDatabaseName)
        {
            var client = new MongoClient(connectionString);
            _database = client.GetDatabase(databaseName);
            _newsDatabase = client.GetDatabase(newsDatabaseName);
        }

        public IMongoCollection<GoldPrice> Prices
        {
            get
            {
                return _database.GetCollection<GoldPrice>("prices");
            }
        }

        public IMongoCollection<DailyPercentage> DailyPercentages
        {
            get
            {
                return _database.GetCollection<DailyPercentage>("daily_percentage");
            }
        }

        public IMongoCollection<User> Users
        {
            get
            {
                return _database.GetCollection<User>("users");
            }
        }

        // Haber desteği için koleksiyon - sadece _newsDatabase tanımlıysa kullanılabilir
        public IMongoCollection<NewsArticle> NewsArticles
        {
            get
            {
                if (_newsDatabase == null)
                {
                    throw new InvalidOperationException("News database is not configured. Use the three-parameter constructor to enable news support.");
                }
                return _newsDatabase.GetCollection<NewsArticle>("news_articles");
            }
        }
    }
}