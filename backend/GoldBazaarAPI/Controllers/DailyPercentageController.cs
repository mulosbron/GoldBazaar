using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using GoldBazaarAPI.Models;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/daily-percentages")]
    public class DailyPercentageController(MongoDBContext context) : ControllerBase
    {
        private readonly IMongoCollection<DailyPercentage> _dailyPercentages = context.DailyPercentages;

        [HttpGet("latest")]
        public async Task<IActionResult> GetLatestDailyPercentage()
        {
            var latestDailyPercentage = await _dailyPercentages.Find(_ => true)
                                                                        .SortByDescending(dp => dp.Id)
                                                                        .FirstOrDefaultAsync();

            if (latestDailyPercentage == null)
            {
                return NotFound(new { Message = "No daily percentage changes were found." });
            }

            return Ok(latestDailyPercentage.PercentageDifference);
        }

        [HttpGet("latest/{product}")]
        public async Task<IActionResult> GetLatestDailyPercentageProduct(string product)
        {
            var latestDailyPercentageDoc = await _dailyPercentages.Find(_ => true)
                                                                           .SortByDescending(dp => dp.Id)
                                                                           .FirstOrDefaultAsync();

            if (latestDailyPercentageDoc != null && latestDailyPercentageDoc.PercentageDifference.TryGetValue(product, out var percentageDifference))
            {
                return Ok(percentageDifference);
            }

            return NotFound(new { Message = $"Product '{product}' was not found in the most recent daily percentage changes document." });
        }
    }
}