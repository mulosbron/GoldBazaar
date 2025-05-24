using GoldBazaar.Application.DTOs;

namespace GoldBazaar.Application.Interfaces
{
    public interface IGoldPriceService
    {
        Task<GoldPriceDto?> GetLatestAsync();
    }
}
