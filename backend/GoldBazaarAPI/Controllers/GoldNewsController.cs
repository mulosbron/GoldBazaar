using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using GoldBazaarAPI.Models;
using GoldBazaarAPI.Data;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Text.RegularExpressions;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/gold-news")]
    public class GoldNewsController : ControllerBase
    {
        private readonly IMongoCollection<NewsArticle> _newsArticles;

        public GoldNewsController(MongoDBContext context)
        {
            _newsArticles = context.NewsArticles;
        }

        [HttpGet]
        public async Task<ActionResult<NewsResponse>> GetGoldNews(
            // Query ve language parametrelerini kaldırıyoruz
            [FromQuery] string sortBy = "publishedAt",
            [FromQuery] int pageSize = 20,
            [FromQuery] int page = 1)
        {
            try
            {
                // Sayfalama için hesaplamalar
                int skip = (page - 1) * pageSize;

                // Sıralama için sorter oluştur
                var sort = sortBy?.ToLower() switch
                {
                    "popularity" => Builders<NewsArticle>.Sort.Descending(n => n.Source.Name),
                    "relevancy" => Builders<NewsArticle>.Sort.Descending(n => n.Title),
                    _ => Builders<NewsArticle>.Sort.Descending(n => n.PublishedAt) // Varsayılan: "publishedAt"
                };

                // Tüm haberleri getir (filter kaldırıldı)
                var totalResults = await _newsArticles.CountDocumentsAsync(FilterDefinition<NewsArticle>.Empty);

                // Haberleri getir
                var articles = await _newsArticles
                    .Find(FilterDefinition<NewsArticle>.Empty)
                    .Sort(sort)
                    .Skip(skip)
                    .Limit(pageSize)
                    .ToListAsync();

                // Yanıt nesnesini oluştur
                var response = new NewsResponse
                {
                    Status = "ok",
                    TotalResults = (int)totalResults,
                    Articles = articles
                };

                return Ok(response);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { status = "error", message = ex.Message });
            }
        }
    }
}