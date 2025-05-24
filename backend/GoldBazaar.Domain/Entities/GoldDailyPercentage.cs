using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace GoldBazaar.Domain.Entities
{
    public class GoldDailyPercentage : BaseEntity
    {
        [BsonElement("date")]
        public string Date { get; set; } = default!;

        [BsonElement("percentage_difference")]
        public Dictionary<string, PercentageChange> PercentageDifference { get; set; } = new();
    }

    public class PercentageChange
    {
        [BsonElement("Alış Fiyatı")]
        public decimal BuyingPrice { get; set; }

        [BsonElement("Satış Fiyatı")]
        public decimal SellingPrice { get; set; }
    }
}
