using GoldBazaarAPI.Models;
using MongoDB.Driver;

namespace GoldBazaarAPI.Services
{
    public class UserCreation
    {
        public User CreateUniqueUser(IMongoCollection<User> users, string email, string passwordHash)
        {
            string username;
            User existingUserByUsername;
            Random random = new();

            do
            {
                username = $"user{random.Next(100000, 999999)}";
                existingUserByUsername = users.Find(u => u.Username == username).FirstOrDefault();
            }
            while (existingUserByUsername != null);

            var user = new User
            {
                Email = email,
                PasswordHash = passwordHash,
                Username = username
            };

            return user;
        }
    }
}
