namespace GoldBazaar.Application.DTOs;

public class NewsArticleDto
{
    public string Title { get; set; } = default!;
    public string Url { get; set; } = default!;
    public string? Author { get; set; }
    public string? Description { get; set; }
    public string? UrlToImage { get; set; }
    public DateTime PublishedAt { get; set; }
    public string SourceName { get; set; } = default!;
}