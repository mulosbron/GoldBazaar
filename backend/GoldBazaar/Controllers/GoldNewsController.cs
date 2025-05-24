using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;

using GoldBazaar.Application.Interfaces;
using GoldBazaar.Application.DTOs;
using GoldBazaar.Shared.Models;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/gold-news")]
    [ApiExplorerSettings(GroupName = "News")]
    public class GoldNewsController : ControllerBase
    {
        private readonly INewsService _newsService;

        public GoldNewsController(IServiceManager services)
        {
            _newsService = services.NewsService;
        }

        /// <summary>
        /// Altınla ilgili haberleri sayfalı & sıralı biçimde döndürür.
        /// </summary>
        [HttpGet]
        public async Task<ActionResult<ApiResponse<PagedList<NewsArticleDto>>>> GetGoldNews(
            [FromQuery] string sortBy = "publishedAt",
            [FromQuery] int pageSize = 20,
            [FromQuery] int page = 1)
        {
            // basit korumalar
            pageSize = pageSize switch { <= 0 => 20, > 100 => 100, _ => pageSize };
            page = page <= 0 ? 1 : page;

            // 1) Haberleri al (ilk aşamada yeterli sayıda)
            var articles = (await _newsService.GetLatestAsync(pageSize * page)).ToList();

            // 2) Sıralama
            IEnumerable<NewsArticleDto> sorted = sortBy.ToLower() switch
            {
                "popularity" => articles.OrderByDescending(a => a.SourceName),
                "relevancy" => articles.OrderByDescending(a => a.Title),
                _ => articles.OrderByDescending(a => a.PublishedAt)
            };

            // 3) Sayfalama
            int totalCount = sorted.Count();
            var pageItems = sorted
                             .Skip((page - 1) * pageSize)
                             .Take(pageSize);

            var paged = new PagedList<NewsArticleDto>(pageItems, totalCount, page, pageSize);

            // 4) Standart API cevabı
            return Ok(ApiResponse<PagedList<NewsArticleDto>>.Ok(paged));
        }
    }
}
