using GoldBazaar.Application.Interfaces;

public interface IServiceManager
{
    IGoldPriceService GoldPriceService { get; }
    IGoldDailyPercentageService GoldDailyPercentageService { get; }
    INewsService NewsService { get; }
    IReportService ReportService { get; }
}
