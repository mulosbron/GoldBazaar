using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace GoldBazaarAPI.Models
{
    public class User
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        [BsonElement("Email")]
        public required string Email { get; set; }

        [BsonElement("PasswordHash")]
        public required string PasswordHash { get; set; }

        [BsonElement("Username")]
        public required string Username { get; set; }

        [BsonElement("Role")]
        public string Role { get; set; } = "user";

        [BsonElement("ResetToken")]
        public string? ResetToken { get; set; }

        [BsonElement("ResetTokenExpires")]
        public DateTime? ResetTokenExpires { get; set; }

        [BsonElement("BannedUntil")]
        public DateTime? BannedUntil { get; set; }
    }
}