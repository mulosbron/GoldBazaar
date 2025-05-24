using Microsoft.AspNetCore.Mvc;

using GoldBazaar.Application.Interfaces;
using GoldBazaar.Domain.Interfaces;      // IRepositoryManager
using GoldBazaar.Shared.Models;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/reports")]
    [ApiExplorerSettings(GroupName = "Reports")]
    public class ReportController : ControllerBase
    {
        private readonly IRepositoryManager _repo;
        private readonly IReportService _reportService;

        public ReportController(IRepositoryManager repo,
                                IServiceManager svcManager)
        {
            _repo = repo;
            _reportService = svcManager.ReportService;
        }

        /// <summary>Son fiyat verilerinden CSV / PDF / Excel raporu üretir.</summary>
        [HttpGet("download/{fileType}")]
        public async Task<IActionResult> Download(string fileType)
        {
            var entity = await _repo.GoldPrices.GetLatestAsync();
            if (entity is null)
                return NotFound(ApiResponse<string>.Fail("No gold price data found."));

            var table = _reportService.BuildGoldPriceTable(new[] { entity });

            return fileType.ToLower() switch
            {
                "csv" => _reportService.GenerateCsv(table, "gold-price-report"),
                //"excel" => _reportService.GenerateExcel(table, "gold-price-report"),
                "pdf" => _reportService.GeneratePdf(table, "gold-price-report"),
                _ => BadRequest(ApiResponse<string>.Fail("fileType must be csv or pdf"))
            };
        }
    }
}
