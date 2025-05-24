using GoldBazaar.Domain.Entities;
using MongoDB.Driver;

namespace GoldBazaar.Domain.Interfaces;

public interface IMongoContext
{
    IMongoDatabase Database { get; }
    IMongoCollection<GoldPrice> Prices { get; }
    IMongoCollection<GoldDailyPercentage> DailyPercentages { get; }
    IMongoCollection<NewsArticle> NewsArticles { get; }
}
