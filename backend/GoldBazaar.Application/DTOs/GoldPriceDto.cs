namespace GoldBazaar.Application.DTOs
{
    public class GoldPriceDto
    {
        public string Date { get; set; } = default!;
        public Dictionary<string, GoldPriceDetailDto> Data { get; set; } = new();
    }

    public class GoldPriceDetailDto
    {
        public decimal BuyingPrice { get; set; }
        public decimal SellingPrice { get; set; }
    }
}
