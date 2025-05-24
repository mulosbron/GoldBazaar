using FluentValidation;
using GoldBazaar.Application.DTOs;

public class GoldDailyPercentageDtoValidator : AbstractValidator<GoldDailyPercentageDto>
{
    public GoldDailyPercentageDtoValidator()
    {
        RuleFor(x => x.Date).NotEmpty();
        RuleForEach(x => x.PercentageDifference)
            .ChildRules(d =>
            {
                d.RuleFor(v => v.Value.BuyingPrice).InclusiveBetween(-100m, 100m);
                d.RuleFor(v => v.Value.SellingPrice).InclusiveBetween(-100m, 100m);
            });
    }
}