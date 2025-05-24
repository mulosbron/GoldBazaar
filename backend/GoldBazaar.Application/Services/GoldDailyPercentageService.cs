using AutoMapper;
using GoldBazaar.Application.DTOs;
using GoldBazaar.Application.Interfaces;
using GoldBazaar.Domain.Interfaces;

public class GoldDailyPercentageService : IGoldDailyPercentageService
{
    private readonly IRepositoryManager _repo;
    private readonly IMapper _mapper;

    public GoldDailyPercentageService(IRepositoryManager repo, IMapper mapper)
    {
        _repo = repo;
        _mapper = mapper;
    }

    public async Task<GoldDailyPercentageDto?> GetLatestAsync()
    {
        var entity = await _repo.DailyPercentages.GetLatestAsync();
        return entity is null ? null : _mapper.Map<GoldDailyPercentageDto>(entity);
    }
}