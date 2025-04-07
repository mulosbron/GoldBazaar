using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using GoldBazaarAPI.Models;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/gold-prices")]
    public class GoldPricesController(MongoDBContext context) : ControllerBase
    {
        private readonly IMongoCollection<GoldPrice> _goldPrices = context.Prices;

        [HttpGet("latest")]
        public async Task<IActionResult> GetLatestGoldPrice()
        {
            var latestGoldPrice = await _goldPrices.Find(_ => true)
                                                             .SortByDescending(g => g.Id)
                                                             .FirstOrDefaultAsync();

            if (latestGoldPrice == null)
            {
                return NotFound(new { Message = "No gold prices found." });
            }

            return Ok(latestGoldPrice.Data);
        }

        [HttpGet("latest/{product}")]
        public async Task<ActionResult<GoldPriceDetail>> GetLatestGoldPriceProduct(string product)
        {
            var latestGoldPriceDoc = await _goldPrices.Find(_ => true)
                                                                .SortByDescending(g => g.Id)
                                                                .FirstOrDefaultAsync();

            if (latestGoldPriceDoc != null && latestGoldPriceDoc.Data.TryGetValue(product, out var productPrice))
            {
                return Ok(productPrice);
            }

            return NotFound(new { Message = $"Product {product} not found in the latest gold prices document." });
        }

    }
}