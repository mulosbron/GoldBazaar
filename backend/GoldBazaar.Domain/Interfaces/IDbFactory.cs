using MongoDB.Driver;
using GoldBazaar.Domain.Enums;

namespace GoldBazaar.Domain.Interfaces;

public interface IDbFactory
{
    IMongoDatabase GetDatabase(DbType type);
}
