using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace GoldBazaarAPI.Models
{
    public class DailyPercentage
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public required string Id { get; set; }

        [BsonElement("date")]
        public required string Date { get; set; }

        [BsonElement("percentage_difference")]
        public required Dictionary<string, PercentageChange> PercentageDifference { get; set; }
    }

    public class PercentageChange
    {
        [BsonElement("Alış Fiyatı")]
        public double BuyingPrice { get; set; }

        [BsonElement("Satış Fiyatı")]
        public double SellingPrice { get; set; }
    }
}