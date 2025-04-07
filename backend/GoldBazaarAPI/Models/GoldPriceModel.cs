using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace GoldBazaarAPI.Models
{
    public class GoldPrice
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public required string Id { get; set; }

        [BsonElement("date")]
        public required string Date { get; set; }

        [BsonElement("data")]
        public required Dictionary<string, GoldPriceDetail> Data { get; set; }
    }

    public class GoldPriceDetail
    {
        [BsonElement("Alış Fiyatı")]
        public double BuyingPrice { get; set; }

        [BsonElement("Satış Fiyatı")]
        public double SellingPrice { get; set; }
    }
}