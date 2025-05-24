using GoldBazaar.Domain.Interfaces;
using GoldBazaar.Infrastructure.Context;
using MongoDB.Driver;
using System.Linq.Expressions;

namespace GoldBazaar.Infrastructure.Repositories
{
    public class MongoRepositoryBase<T> : IRepositoryBase<T>
    {
        protected readonly IMongoCollection<T> _collection;

        protected MongoRepositoryBase(IMongoDatabase database, string collectionName)
        {
            _collection = database.GetCollection<T>(collectionName);
        }

        public async Task<IEnumerable<T>> GetAllAsync() =>
            await _collection.Find(Builders<T>.Filter.Empty).ToListAsync();

        public async Task<T> GetAsync(string id) =>
            await _collection.Find(Builders<T>.Filter.Eq("_id", id)).FirstOrDefaultAsync();

        public async Task<IEnumerable<T>> FindByConditionAsync(Expression<Func<T, bool>> expression) =>
            await _collection.Find(expression).ToListAsync();

        public async Task CreateAsync(T entity) =>
            await _collection.InsertOneAsync(entity);

        public async Task UpdateAsync(string id, T entity) =>
            await _collection.ReplaceOneAsync(Builders<T>.Filter.Eq("_id", id), entity);

        public async Task DeleteAsync(string id) =>
            await _collection.DeleteOneAsync(Builders<T>.Filter.Eq("_id", id));
    }
}
