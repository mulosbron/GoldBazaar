using MongoDB.Driver;
using GoldBazaarAPI.Models;

public class MongoDBContext
{
    private readonly IMongoDatabase _database;

    public MongoDBContext(string connectionString, string databaseName)
    {
        var client = new MongoClient(connectionString);
        _database = client.GetDatabase(databaseName);
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
}
