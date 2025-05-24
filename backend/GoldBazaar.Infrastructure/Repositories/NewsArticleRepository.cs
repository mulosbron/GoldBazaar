using GoldBazaar.Domain.Entities;
using GoldBazaar.Domain.Enums;
using GoldBazaar.Domain.Interfaces;
using GoldBazaar.Infrastructure.Context;
using MongoDB.Driver;

namespace GoldBazaar.Infrastructure.Repositories;

public class NewsArticleRepository : MongoRepositoryBase<NewsArticle>, INewsArticleRepository
{
    public NewsArticleRepository(IDbFactory factory) : base(factory.GetDatabase(DbType.News), "news_articles") { }

    public async Task<IEnumerable<NewsArticle>> GetLatestAsync(int take = 10)
        => await _collection.Find(_ => true)
                            .SortByDescending(n => n.PublishedAt)
                            .Limit(take)
                            .ToListAsync();
}