namespace GoldBazaar.Domain.Interfaces
{
    public interface IRepositoryManager
    {
        IGoldPriceRepository GoldPrices { get; }
        IGoldDailyPercentageRepository DailyPercentages { get; }
        INewsArticleRepository NewsArticles { get; }
    }
}
