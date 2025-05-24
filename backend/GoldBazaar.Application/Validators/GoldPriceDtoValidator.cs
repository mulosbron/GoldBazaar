using FluentValidation;
using GoldBazaar.Application.DTOs;

namespace GoldBazaar.Application.Validators
{
    public class GoldPriceDtoValidator : AbstractValidator<GoldPriceDto>
    {
        public GoldPriceDtoValidator()
        {
            RuleFor(x => x.Date).NotEmpty();
            RuleForEach(x => x.Data).ChildRules(data =>
            {
                data.RuleFor(d => d.Value.BuyingPrice).GreaterThan(0);
                data.RuleFor(d => d.Value.SellingPrice).GreaterThan(0);
            });
        }
    }
}
