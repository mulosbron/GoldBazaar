using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using GoldBazaarAPI.Models;
using GoldBazaarAPI.Data;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/queries")]
    public class QueryController(MongoDBContext context) : ControllerBase
    {
        private readonly IMongoCollection<GoldPrice> _goldPrices = context.Prices;

        [HttpGet("search/{query}")]
        public async Task<IActionResult> SearchAssets(string query)
        {
            var latestGoldPrice = await _goldPrices.Find(_ => true)
                                                             .SortByDescending(g => g.Id)
                                                             .FirstOrDefaultAsync();

            if (latestGoldPrice == null)
            {
                return NotFound(new { Message = "Gold prices could not be found." });
            }

            var goldNames = latestGoldPrice.Data.Keys;
            var matchedGoldNames = goldNames.Where(name => name.Contains(query, StringComparison.CurrentCultureIgnoreCase)).ToList();

            if (matchedGoldNames.Count == 0)
            {
                return NotFound(new { Message = "No results were found that match the specified criteria." });
            }

            return Ok(matchedGoldNames);
        }
    }
}
