using GoldBazaar.Application.DTOs;

public interface IGoldDailyPercentageService
{
    Task<GoldDailyPercentageDto?> GetLatestAsync();
}