namespace GoldBazaar.Application.DTOs;

public class GoldDailyPercentageDto
{
    public string Date { get; set; } = default!;
    public Dictionary<string, PercentageChangeDto> PercentageDifference { get; set; } = new();
}

public class PercentageChangeDto
{
    public decimal BuyingPrice { get; set; }
    public decimal SellingPrice { get; set; }
}