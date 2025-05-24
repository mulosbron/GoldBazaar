namespace GoldBazaar.Infrastructure.Configurations
{
    public class MongoDbSettings
    {
        public string ConnectionString { get; set; } = default!;
        public string MainDatabaseName { get; set; } = default!;   // gold_prices
        public string NewsDatabaseName { get; set; } = default!;   // gold_news
    }

}
