using AutoMapper;
using GoldBazaar.Application.Mapping;

namespace GoldBazaarAPI.Extensions;

public static class AutoMapperExtensions
{
    public static IServiceCollection ConfigureAutoMapper(this IServiceCollection services)
    {
        services.AddAutoMapper(cfg => cfg.AddProfile<MappingProfile>());
        return services;
    }
}