using FluentValidation;
using GoldBazaar.Application.Interfaces;
using GoldBazaar.Application.Services;
using GoldBazaar.Application.Validators;
using Microsoft.Extensions.DependencyInjection;

namespace GoldBazaar.Application.DependencyInjection;

public static class ApplicationServiceRegistration
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        // Uygulama servisleri
        services.AddScoped<IServiceManager, ServiceManager>();

        // Tüm validator’ları derleme zamanı assembly’sinden çek
        services.AddValidatorsFromAssemblyContaining<GoldPriceDtoValidator>(
            ServiceLifetime.Scoped);

        return services;
    }
}
