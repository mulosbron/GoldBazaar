using Microsoft.AspNetCore.Mvc;
using System.Data;
using GoldBazaar.Domain.Entities;

namespace GoldBazaar.Application.Interfaces
{
    public interface IReportService
    {
        FileContentResult GeneratePdf(DataTable table, string reportName);
        FileContentResult GenerateExcel(DataTable table, string reportName);
        FileContentResult GenerateCsv(DataTable table, string reportName);
        DataTable BuildGoldPriceTable(IEnumerable<GoldPrice> goldPrices);
    }
}
