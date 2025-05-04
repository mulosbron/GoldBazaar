using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using GoldBazaarAPI.Models;
using GoldBazaarAPI.Services;
using GoldBazaarAPI.Data;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/reports")]
    public class ReportController(MongoDBContext context, ReportService reportService) : ControllerBase
    {
        private readonly IMongoCollection<GoldPrice> _goldPrices = context.Prices;
        private readonly ReportService _reportService = reportService;

        [HttpGet("download/{fileType}")]
        public async Task<IActionResult> DownloadReport(string fileType)
        {
            var goldPrices = await _goldPrices.Find(_ => true).ToListAsync();
            var dataTable = _reportService.BuildDataTable(goldPrices);

            return fileType.ToLower() switch
            {
                "pdf" => _reportService.GeneratePdf(dataTable, "GoldPricesReport"),
                "excel" => _reportService.GenerateExcel(dataTable, "GoldPricesReport"),
                "csv" => _reportService.GenerateCsv(dataTable, "GoldPricesReport"),
                _ => BadRequest("Invalid file type requested.")
            };
        }
    }
}
