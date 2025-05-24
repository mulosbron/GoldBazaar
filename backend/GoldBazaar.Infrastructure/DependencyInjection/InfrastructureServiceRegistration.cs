using GoldBazaar.Domain.Interfaces;
using GoldBazaar.Infrastructure.Configurations;
using GoldBazaar.Infrastructure.Context;
using GoldBazaar.Infrastructure.Logging;
using GoldBazaar.Infrastructure.Repositories;
using GoldBazaar.Shared.Interfaces;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using MongoDB.Driver;

namespace GoldBazaar.Infrastructure.DependencyInjection
{
    public static class InfrastructureServiceRegistration
    {
        public static IServiceCollection AddInfrastructure(
            this IServiceCollection services,
            IConfiguration configuration)
        {
            // Mongo ayarlarını oku - options pattern
            services.Configure<MongoDbSettings>(configuration.GetSection("MongoDbSettings"));

            // --- factory ve repos
            services.AddSingleton<IDbFactory, MongoDbFactory>();
            services.AddSingleton<IRepositoryManager, RepositoryManager>();

            // Logging
            services.AddSingleton<ILoggerManager, LoggerManager>();

            return services;
        }
    }
}
