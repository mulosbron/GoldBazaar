using CsvHelper;
using GoldBazaar.Application.Interfaces;
using GoldBazaar.Domain.Entities;
using iTextSharp.text;
using iTextSharp.text.pdf;
using Microsoft.AspNetCore.Mvc;
using OfficeOpenXml;
using System.Data;
using System.Globalization;
using System.Text;


namespace GoldBazaar.Application.Services
{
    public class ReportService : IReportService
    {
        public DataTable BuildGoldPriceTable(IEnumerable<GoldPrice> goldPrices)
        {
            var table = new DataTable("GoldPrices");
            table.Columns.Add("Date", typeof(string));
            table.Columns.Add("Gold Type", typeof(string));
            table.Columns.Add("Buying Price", typeof(double));
            table.Columns.Add("Selling Price", typeof(double));

            foreach (var price in goldPrices)
            {
                foreach (var entry in price.Data)
                {
                    table.Rows.Add(price.Date, entry.Key, entry.Value.BuyingPrice, entry.Value.SellingPrice);
                }
            }

            return table;
        }

        public FileContentResult GeneratePdf(DataTable table, string reportName)
        {
            using var stream = new MemoryStream();
            using var doc = new Document(PageSize.A4, 10f, 10f, 10f, 10f);
            PdfWriter.GetInstance(doc, stream);
            doc.Open();

            var font = new Font(BaseFont.CreateFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), 12);

            var pdfTable = new PdfPTable(table.Columns.Count) { WidthPercentage = 100 };

            foreach (DataColumn column in table.Columns)
                pdfTable.AddCell(new PdfPCell(new Phrase(column.ColumnName, font)));

            foreach (DataRow row in table.Rows)
            {
                foreach (var cell in row.ItemArray)
                    pdfTable.AddCell(new PdfPCell(new Phrase(cell?.ToString(), font)));
            }

            doc.Add(pdfTable);
            doc.Close();

            return new FileContentResult(stream.ToArray(), "application/pdf")
            {
                FileDownloadName = $"{reportName}.pdf"
            };
        }

        public FileContentResult GenerateExcel(DataTable table, string reportName)
        {
            ExcelPackage.LicenseContext = LicenseContext.NonCommercial;
            using var package = new ExcelPackage();
            var sheet = package.Workbook.Worksheets.Add("GoldPrices");
            sheet.Cells["A1"].LoadFromDataTable(table, true);
            var stream = new MemoryStream(package.GetAsByteArray());

            return new FileContentResult(stream.ToArray(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            {
                FileDownloadName = $"{reportName}.xlsx"
            };
        }

        public FileContentResult GenerateCsv(DataTable table, string reportName)
        {
            using var memoryStream = new MemoryStream();
            using var writer = new StreamWriter(memoryStream, new UTF8Encoding(true));
            using var csv = new CsvWriter(writer, CultureInfo.InvariantCulture);

            foreach (DataColumn column in table.Columns)
                csv.WriteField(column.ColumnName);
            csv.NextRecord();

            foreach (DataRow row in table.Rows)
            {
                foreach (var cell in row.ItemArray)
                    csv.WriteField(cell);
                csv.NextRecord();
            }

            writer.Flush();
            memoryStream.Position = 0;

            return new FileContentResult(memoryStream.ToArray(), "text/csv")
            {
                FileDownloadName = $"{reportName}.csv"
            };
        }
    }
}
