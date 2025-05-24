using GoldBazaar.Domain.Entities;

namespace GoldBazaar.Domain.Interfaces;

public interface IGoldDailyPercentageRepository : IRepositoryBase<GoldDailyPercentage>
{
    Task<GoldDailyPercentage?> GetLatestAsync();
}