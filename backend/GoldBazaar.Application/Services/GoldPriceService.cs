using AutoMapper;
using GoldBazaar.Application.DTOs;
using GoldBazaar.Application.Interfaces;
using GoldBazaar.Domain.Interfaces;

namespace GoldBazaar.Application.Services
{
    public class GoldPriceService : IGoldPriceService
    {
        private readonly IRepositoryManager _repo;
        private readonly IMapper _mapper;

        public GoldPriceService(IRepositoryManager repo, IMapper mapper)
        {
            _repo = repo;
            _mapper = mapper;
        }

        public async Task<GoldPriceDto?> GetLatestAsync()
        {
            var entity = await _repo.GoldPrices.GetLatestAsync();
            return entity is not null ? _mapper.Map<GoldPriceDto>(entity) : null;
        }
    }
}
