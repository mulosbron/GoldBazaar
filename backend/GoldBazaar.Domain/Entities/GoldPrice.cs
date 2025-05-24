using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace GoldBazaar.Domain.Entities
{
    public class GoldPrice : BaseEntity
    {

        [BsonElement("date")]
        public string Date { get; set; } = default!;

        [BsonElement("data")]
        public Dictionary<string, GoldPriceDetail> Data { get; set; } = new();
    }

    public class GoldPriceDetail
    {
        [BsonElement("Alış Fiyatı")]
        public decimal BuyingPrice { get; set; }

        [BsonElement("Satış Fiyatı")]
        public decimal SellingPrice { get; set; }
    }
}
