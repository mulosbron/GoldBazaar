using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;

using GoldBazaar.Application.Interfaces;
using GoldBazaar.Application.DTOs;
using GoldBazaar.Shared.Models;
using Microsoft.AspNetCore.OutputCaching;
using Amazon.Runtime.Internal.Util;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/gold-prices")]
    [ApiExplorerSettings(GroupName = "Gold")]
    public class GoldPricesController : ControllerBase
    {
        private readonly IGoldPriceService _service;

        public GoldPricesController(IServiceManager manager)
        {
            _service = manager.GoldPriceService;
        }

        /// <summary>
        /// Tüm ürünler için en son altın fiyatları.
        /// </summary>
        [HttpGet("latest")]
        // [OutputCache(NoCache = true)] -> cache etkisiz bırakmak.
        // [OutputCache(PolicyName = "GoldPricesLatest")] belirli bir endpointe uygulama
        public async Task<ActionResult<ApiResponse<GoldPriceDto>>> GetLatest()
        {
            var dto = await _service.GetLatestAsync();
            return dto is null
                ? NotFound(ApiResponse<string>.Fail("No gold prices found."))
                : Ok(ApiResponse<GoldPriceDto>.Ok(dto));
        }

        /// <summary>
        /// Belirli bir ürün için en son altın fiyatı.
        /// </summary>
        [HttpGet("latest/{product}")]
        public async Task<ActionResult<ApiResponse<GoldPriceDetailDto>>> GetLatestForProduct(string product)
        {
            var dto = await _service.GetLatestAsync();

            if (dto is null || !dto.Data.TryGetValue(product, out var detail))
                return NotFound(ApiResponse<string>.Fail($"Product '{product}' not found."));

            return Ok(ApiResponse<GoldPriceDetailDto>.Ok(detail));
        }
    }
}
