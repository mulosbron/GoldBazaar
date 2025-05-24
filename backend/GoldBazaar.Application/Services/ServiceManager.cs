using AutoMapper;
using GoldBazaar.Application.Interfaces;
using GoldBazaar.Application.Services;
using GoldBazaar.Domain.Interfaces;

public class ServiceManager : IServiceManager
{
    private readonly Lazy<IGoldPriceService> _goldPrice;
    private readonly Lazy<IGoldDailyPercentageService> _dailyPct;
    private readonly Lazy<INewsService> _news;
    private readonly Lazy<IReportService> _report;

    public ServiceManager(IRepositoryManager repo, IMapper mapper)
    {
        _goldPrice = new(() => new GoldPriceService(repo, mapper));
        _dailyPct = new(() => new GoldDailyPercentageService(repo, mapper));
        _news = new(() => new NewsService(repo, mapper));
        _report = new(() => new ReportService());
    }

    public IGoldPriceService GoldPriceService => _goldPrice.Value;
    public IGoldDailyPercentageService GoldDailyPercentageService => _dailyPct.Value;
    public INewsService NewsService => _news.Value;
    public IReportService ReportService => _report.Value;
}
