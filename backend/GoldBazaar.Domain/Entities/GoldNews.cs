using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace GoldBazaar.Domain.Entities
{
    public class NewsArticle : BaseEntity
    {

        [BsonElement("source")]
        public NewsSource Source { get; set; } = new();

        [BsonElement("author")]
        public string? Author { get; set; }

        [BsonElement("title")]
        public string Title { get; set; } = default!;

        [BsonElement("description")]
        public string? Description { get; set; }

        [BsonElement("url")]
        public string Url { get; set; } = default!;

        [BsonElement("urlToImage")]
        public string? UrlToImage { get; set; }

        [BsonElement("publishedAt"), BsonDateTimeOptions(Kind = DateTimeKind.Utc)]
        public DateTime PublishedAt { get; set; }

        [BsonElement("content")]
        public string? Content { get; set; }

        [BsonElement("scrapedAt"), BsonDateTimeOptions(Kind = DateTimeKind.Utc)]
        public DateTime ScrapedAt { get; set; }
    }

    [BsonIgnoreExtraElements]
    public class NewsSource
    {
        [BsonElement("id")]
        public string? Id { get; set; }

        [BsonElement("name")]
        public string Name { get; set; } = default!;
    }
}
