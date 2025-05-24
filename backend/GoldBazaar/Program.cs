using GoldBazaarAPI.Extensions;
using GoldBazaarAPI.Middleware;
using GoldBazaar.Filters;

using GoldBazaar.Application.DependencyInjection;
using GoldBazaar.Application.Interfaces;
using GoldBazaar.Application.Services;

using GoldBazaar.Infrastructure.DependencyInjection;

using FluentValidation.AspNetCore;      // AutoValidation & ClientSideAdapters
using NLog;
using AspNetCoreRateLimit;

var logger = LogManager.Setup()
                       .LoadConfigurationFromFile("nlog.config")
                       .GetCurrentClassLogger();

logger.Info("Application Starting…");

try
{
    var builder = WebApplication.CreateBuilder(args);

    //──────── Controllers + FluentValidation ────────
    builder.Services.AddControllers(o => o.Filters.Add<ValidationFilterAttribute>());

    builder.Services.AddFluentValidationAutoValidation();      // v11 ⇒ otomatik model doğrulama
    builder.Services.AddFluentValidationClientsideAdapters();  // swagger/js için öznitelikler

    //──────── Cache ───────────────
    //builder.Services.AddMemoryCache();
    //builder.Services.AddOutputCache(opt => {
    //    opt.AddPolicy("GoldPricesLatest", 
    //    p => p.Expire(TimeSpan.FromSeconds(60)));
    //});
    //──────── Cache ───────────────
    builder.Services.AddMemoryCache();

    builder.Services.AddOutputCache(opt =>
    {
        // Tüm 200-lü GET/HEAD yanıtlarını 60 sn cache’le
        opt.AddBasePolicy(b => b
            .Expire(TimeSpan.FromSeconds(60))
            .Cache()              // sadece GET/HEAD, 200 status
            .SetVaryByQuery("*"));   // query-string farkına göre ayrı anahtar
    });



    //──────── Rate-Limit (Redis store) ───────────────
    builder.Services.Configure<IpRateLimitOptions>(
        builder.Configuration.GetSection("IpRateLimiting"));
    builder.Services.Configure<IpRateLimitPolicies>(
        builder.Configuration.GetSection("IpRateLimitPolicies"));
    builder.Services.AddInMemoryRateLimiting(); 
    builder.Services.AddSingleton<IRateLimitConfiguration, RateLimitConfiguration>();


    //──────── AutoMapper ────────
    builder.Services.ConfigureAutoMapper();

    //──────── Katman bağımlılıkları ────────
    builder.Services.AddInfrastructure(builder.Configuration); // Mongo, Logger, Repos…
    builder.Services.AddApplication();                         // ServiceManager + Validators
    builder.Services.AddScoped<IReportService, ReportService>();

    //──────── Swagger + CORS ────────
    builder.Services.AddGroupedSwagger();
    builder.Services.ConfigureCors();

    //──────── Pipeline ────────
    var app = builder.Build();

    app.UseMiddleware<GlobalExceptionMiddleware>();

    app.UseGroupedSwaggerUI();
    app.UseCors("AllowAll");

    // app.UseHttpsRedirection();   // gerekirse aç
    app.UseAuthentication();
    app.UseAuthorization();

    app.MapControllers();
    app.Run();
}
catch (Exception ex)
{
    logger.Error(ex, "Application stopped due to an exception.");
    throw;
}
finally
{
    LogManager.Shutdown();
}
