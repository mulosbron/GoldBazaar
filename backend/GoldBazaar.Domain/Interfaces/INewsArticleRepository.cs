using GoldBazaar.Domain.Entities;

namespace GoldBazaar.Domain.Interfaces;

public interface INewsArticleRepository : IRepositoryBase<NewsArticle>
{
    Task<IEnumerable<NewsArticle>> GetLatestAsync(int take = 10);
}