using AutoMapper;
using GoldBazaar.Application.DTOs;
using GoldBazaar.Application.Interfaces;
using GoldBazaar.Domain.Interfaces;

public class NewsService : INewsService
{
    private readonly IRepositoryManager _repo;
    private readonly IMapper _mapper;

    public NewsService(IRepositoryManager repo, IMapper mapper)
    {
        _repo = repo;
        _mapper = mapper;
    }

    public async Task<IEnumerable<NewsArticleDto>> GetLatestAsync(int take = 10)
    {
        var articles = await _repo.NewsArticles.GetLatestAsync(take);
        return _mapper.Map<IEnumerable<NewsArticleDto>>(articles);
    }
}