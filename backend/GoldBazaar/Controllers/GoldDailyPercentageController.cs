using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;

using GoldBazaar.Application.Interfaces;
using GoldBazaar.Shared.Models;
using GoldBazaar.Application.DTOs;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/gold-daily-percentages")]
    [ApiExplorerSettings(GroupName = "Gold")]
    public class GoldDailyPercentageController : ControllerBase
    {
        private readonly IGoldDailyPercentageService _service;

        public GoldDailyPercentageController(IServiceManager manager)
        {
            _service = manager.GoldDailyPercentageService;
        }

        /// <summary>
        /// Tüm ürünler için en son günlük yüzde değişimleri.
        /// </summary>
        [HttpGet("latest")]
        public async Task<ActionResult<ApiResponse<GoldDailyPercentageDto>>> GetLatest()
        {
            var dto = await _service.GetLatestAsync();
            return dto is null
                ? NotFound(ApiResponse<string>.Fail("No daily percentage data found."))
                : Ok(ApiResponse<GoldDailyPercentageDto>.Ok(dto));
        }

        /// <summary>
        /// Belirli bir ürün için en son günlük yüzde değişimi.
        /// </summary>
        [HttpGet("latest/{product}")]
        public async Task<ActionResult<ApiResponse<PercentageChangeDto>>> GetLatestForProduct(string product)
        {
            var dto = await _service.GetLatestAsync();
            if (dto is null || !dto.PercentageDifference.TryGetValue(product, out var pct))
                return NotFound(ApiResponse<string>.Fail($"Product '{product}' not found."));

            return Ok(ApiResponse<PercentageChangeDto>.Ok(pct));
        }
    }
}
