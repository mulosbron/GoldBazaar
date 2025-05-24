using Microsoft.OpenApi.Models;

public static class SwaggerExtensions
{
    public static void AddGroupedSwagger(this IServiceCollection services)
    {
        services.AddSwaggerGen(options =>
        {
            options.SwaggerDoc("Gold", new OpenApiInfo { Title = "Gold Prices API", Version = "v1" });
            options.SwaggerDoc("News", new OpenApiInfo { Title = "News API", Version = "v1" });
            options.SwaggerDoc("Reports", new OpenApiInfo { Title = "Reports API", Version = "v1" });

            // Diğer ayarlar örn: açıklama, başlık vs.
        });
    }

    public static void UseGroupedSwaggerUI(this IApplicationBuilder app)
    {
        app.UseSwagger();
        app.UseSwaggerUI(options =>
        {
            options.SwaggerEndpoint("/swagger/Gold/swagger.json", "Gold Prices API");
            options.SwaggerEndpoint("/swagger/News/swagger.json", "News API");
            options.SwaggerEndpoint("/swagger/Reports/swagger.json", "Reports API");
        });
    }
}
