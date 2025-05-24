using GoldBazaar.Domain.Entities;

namespace GoldBazaar.Domain.Interfaces
{
    public interface IGoldPriceRepository : IRepositoryBase<GoldPrice>
    {
        Task<GoldPrice?> GetLatestAsync();
    }
}
