using GoldBazaar.Application.DTOs;

public interface INewsService
{
    Task<IEnumerable<NewsArticleDto>> GetLatestAsync(int take = 10);
}