using AutoMapper;
using GoldBazaar.Domain.Entities;
using GoldBazaar.Application.DTOs;

namespace GoldBazaar.Application.Mapping;

public class MappingProfile : Profile
{
    public MappingProfile()
    {
        CreateMap<GoldPrice, GoldPriceDto>().ReverseMap();
        CreateMap<GoldPriceDetail, GoldPriceDetailDto>().ReverseMap();

        CreateMap<GoldDailyPercentage, GoldDailyPercentageDto>().ReverseMap();
        CreateMap<PercentageChange, PercentageChangeDto>().ReverseMap();

        CreateMap<NewsArticle, NewsArticleDto>()
            .ForMember(d => d.SourceName, o => o.MapFrom(s => s.Source.Name));
    }
}
