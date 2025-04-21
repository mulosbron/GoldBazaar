using System.Data;
using System.Formats.Asn1;
using System.Globalization;
using System.Text;
using CsvHelper;
using GoldBazaarAPI.Models;
using iTextSharp.text;
using iTextSharp.text.pdf;
using Microsoft.AspNetCore.Mvc;
using OfficeOpenXml;
using static System.Net.Mime.MediaTypeNames;
using Font = iTextSharp.text.Font;

namespace GoldBazaarAPI.Services
{
    public class ReportService
    {

        public FileContentResult GeneratePdf(DataTable dataTable, string reportName)
        {
            if (dataTable == null || dataTable.Rows.Count == 0)
            {
                throw new InvalidOperationException("The provided DataTable is null or empty.");
            }

            using var stream = new MemoryStream();
            using var pdfDoc = new Document(PageSize.A4, 10f, 10f, 10f, 0f);
            PdfWriter.GetInstance(pdfDoc, stream);
            pdfDoc.Open();

            var baseFont = BaseFont.CreateFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            var font = new Font(baseFont, 12, Font.NORMAL);

            var pdfTable = new PdfPTable(dataTable.Columns.Count);
            pdfTable.WidthPercentage = 100;

            AddTableHeaders(dataTable, pdfTable, font);
            AddTableData(dataTable, pdfTable, font);

            if (pdfTable.Rows.Count > 0)
            {
                pdfDoc.Add(pdfTable);
            }
            else
            {
                throw new InvalidOperationException("No content was added to the PDF document.");
            }

            pdfDoc.Close();

            return new FileContentResult(stream.ToArray(), "application/pdf") { FileDownloadName = $"{reportName}.pdf" };
        }

        public FileContentResult GenerateExcel(DataTable dataTable, string reportName)
        {
            using var package = new ExcelPackage();
            var worksheet = package.Workbook.Worksheets.Add("Report");
            worksheet.Cells["A1"].LoadFromDataTable(dataTable, PrintHeaders: true);
            var stream = new MemoryStream(package.GetAsByteArray());

            return new FileContentResult(stream.ToArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") { FileDownloadName = $"{reportName}.xlsx" };
        }

        public FileContentResult GenerateCsv(DataTable dataTable, string reportName)
        {
            using var memoryStream = new MemoryStream();
            using var streamWriter = new StreamWriter(memoryStream, new UTF8Encoding(true)); // BOM ile UTF-8
            using var csvWriter = new CsvWriter(streamWriter, CultureInfo.InvariantCulture);

            foreach (DataColumn column in dataTable.Columns)
            {
                csvWriter.WriteField(column.ColumnName);
            }
            csvWriter.NextRecord();

            foreach (DataRow row in dataTable.Rows)
            {
                foreach (var cell in row.ItemArray)
                {
                    csvWriter.WriteField(cell);
                }
                csvWriter.NextRecord();
            }

            streamWriter.Flush();
            memoryStream.Position = 0;

            return new FileContentResult(memoryStream.ToArray(), "text/csv") { FileDownloadName = $"{reportName}.csv" };
        }

        public DataTable BuildDataTable(IEnumerable<GoldPrice> goldPrices)
        {
            var dataTable = new DataTable("GoldPrices");
            dataTable.Columns.AddRange(new[]
            {
                new DataColumn("Date", typeof(string)),
                new DataColumn("Gold Type", typeof(string)),
                new DataColumn("Buying Price", typeof(double)),
                new DataColumn("Selling Price", typeof(double))
            });

            foreach (var goldPrice in goldPrices)
            {
                foreach (var detail in goldPrice.Data)
                {
                    dataTable.Rows.Add(goldPrice.Date, detail.Key, detail.Value.BuyingPrice, detail.Value.SellingPrice);
                }
            }

            return dataTable;
        }

        private static void AddTableHeaders(DataTable dataTable, PdfPTable pdfTable, Font font)
        {
            foreach (DataColumn column in dataTable.Columns)
            {
                var cell = new PdfPCell(new Phrase(column.ColumnName, font));
                pdfTable.AddCell(cell);
            }
        }

        private static void AddTableData(DataTable dataTable, PdfPTable pdfTable, Font font)
        {
            foreach (DataRow row in dataTable.Rows)
            {
                foreach (var cell in row.ItemArray)
                {
                    var phrase = new Phrase(cell?.ToString(), font);
                    pdfTable.AddCell(phrase);
                }
            }
        }
    }
}