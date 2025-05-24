using GoldBazaar.Domain.Interfaces;
using GoldBazaar.Domain.Entities;
using MongoDB.Driver;
using GoldBazaar.Domain.Enums;

namespace GoldBazaar.Infrastructure.Repositories
{
    public class GoldPriceRepository : MongoRepositoryBase<GoldPrice>, IGoldPriceRepository
    {
        public GoldPriceRepository(IDbFactory factory) : base(factory.GetDatabase(DbType.Main), "prices") { }


        public async Task<GoldPrice?> GetLatestAsync()
        {
            return await _collection.Find(_ => true)
                .SortByDescending(p => p.Date)
                .FirstOrDefaultAsync();
        }
    }
}
