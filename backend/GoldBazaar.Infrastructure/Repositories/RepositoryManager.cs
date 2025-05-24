using GoldBazaar.Domain.Interfaces;
using MongoDB.Driver;

namespace GoldBazaar.Infrastructure.Repositories
{
    public class RepositoryManager : IRepositoryManager
    {
        private readonly IDbFactory _factory;

        private IGoldPriceRepository? _goldPrices;
        private IGoldDailyPercentageRepository? _dailyPct;
        private INewsArticleRepository? _news;

        public RepositoryManager(IDbFactory factory) => _factory = factory;

        public IGoldPriceRepository GoldPrices =>
            _goldPrices ??= new GoldPriceRepository(_factory);

        public IGoldDailyPercentageRepository DailyPercentages =>
            _dailyPct ??= new GoldDailyPercentageRepository(_factory);

        public INewsArticleRepository NewsArticles =>
            _news ??= new NewsArticleRepository(_factory);
    }

}
