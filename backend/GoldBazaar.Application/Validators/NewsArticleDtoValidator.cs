using FluentValidation;
using GoldBazaar.Application.DTOs;

public class NewsArticleDtoValidator : AbstractValidator<NewsArticleDto>
{
    public NewsArticleDtoValidator()
    {
        RuleFor(x => x.Title).NotEmpty();

        RuleFor(x => x.Url)
            .NotEmpty()
            .Must(url => Uri.IsWellFormedUriString(url, UriKind.Absolute))
            .WithMessage("Url must be a valid absolute URI.");

        RuleFor(x => x.PublishedAt)
            .LessThanOrEqualTo(DateTime.UtcNow)
            .WithMessage("Published date cannot be in the future.");
    }
}
