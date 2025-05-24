using GoldBazaar.Domain.Entities;
using GoldBazaar.Domain.Enums;
using GoldBazaar.Domain.Interfaces;
using MongoDB.Driver;

namespace GoldBazaar.Infrastructure.Repositories;

public class GoldDailyPercentageRepository
        : MongoRepositoryBase<GoldDailyPercentage>, IGoldDailyPercentageRepository
{
    public GoldDailyPercentageRepository(IDbFactory factory)
        : base(factory.GetDatabase(DbType.Main), "daily_percentage") { }

    public async Task<GoldDailyPercentage?> GetLatestAsync()
        => await _collection.Find(_ => true)
                            .SortByDescending(p => p.Date)
                            .FirstOrDefaultAsync();
}