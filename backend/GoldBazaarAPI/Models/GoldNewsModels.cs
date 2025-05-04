using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;
using System.Collections.Generic;

namespace GoldBazaarAPI.Models
{
    public class NewsArticle
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        [BsonElement("source")]
        public NewsSource Source { get; set; }

        [BsonElement("author")]
        public string Author { get; set; }

        [BsonElement("title")]
        public string Title { get; set; }

        [BsonElement("description")]
        public string Description { get; set; }

        [BsonElement("url")]
        public string Url { get; set; }

        [BsonElement("urlToImage")]
        public string UrlToImage { get; set; }

        [BsonElement("publishedAt")]
        [BsonDateTimeOptions(Kind = DateTimeKind.Utc)]
        public DateTime PublishedAt { get; set; }

        [BsonElement("content")]
        public string Content { get; set; }

        [BsonElement("scrapedAt")]
        [BsonDateTimeOptions(Kind = DateTimeKind.Utc)]
        public DateTime ScrapedAt { get; set; }
    }

    [BsonIgnoreExtraElements]
    public class NewsSource
    {
        // Id özelliğini kaldırıyoruz
        [BsonElement("name")]
        public string Name { get; set; }
    }

    public class NewsResponse
    {
        public string Status { get; set; } = "ok";
        public int TotalResults { get; set; }
        public List<NewsArticle> Articles { get; set; }
    }
}