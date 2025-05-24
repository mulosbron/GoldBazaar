using GoldBazaar.Domain.Enums;
using GoldBazaar.Domain.Interfaces;
using GoldBazaar.Infrastructure.Configurations;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace GoldBazaar.Infrastructure.Context;

/// <summary>
/// Tüm IMongoDatabase örneklerini tek noktadan oluşturur ve önbelleğe alır.
/// </summary>
public class MongoDbFactory : IDbFactory
{
    private readonly MongoClient _client;
    private readonly MongoDbSettings _settings;
    private readonly Dictionary<DbType, IMongoDatabase> _cache = new();

    public MongoDbFactory(IOptions<MongoDbSettings> opt)
    {
        _settings = opt.Value;
        _client = new MongoClient(_settings.ConnectionString);
    }

    public IMongoDatabase GetDatabase(DbType type)
    {
        if (_cache.TryGetValue(type, out var db))
            return db;

        db = type switch
        {
            DbType.News => _client.GetDatabase(_settings.NewsDatabaseName),
            _ => _client.GetDatabase(_settings.MainDatabaseName)
        };

        _cache[type] = db;
        return db;
    }
}
