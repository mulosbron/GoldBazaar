using GoldBazaar.Shared.Interfaces;
using GoldBazaar.Shared.Models;
using System.Net;
using System.Text.Json;

namespace GoldBazaarAPI.Middleware
{
    public class GlobalExceptionMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly ILoggerManager _logger;

        public GlobalExceptionMiddleware(RequestDelegate next, ILoggerManager logger)
        {
            _next = next;
            _logger = logger;
        }

        public async Task InvokeAsync(HttpContext context)
        {
            try
            {
                await _next(context);
            }
            catch (Exception ex)
            {
                _logger.LogError($"[GlobalExceptionMiddleware] {ex.Message}\n{ex.StackTrace}");

                context.Response.ContentType = "application/json";
                context.Response.StatusCode = (int)HttpStatusCode.InternalServerError;

                var error = new ErrorDetails
                {
                    StatusCode = context.Response.StatusCode,
                    Message = ex.Message // Burada artık gerçek hata mesajını göreceksin
                };

                var json = JsonSerializer.Serialize(error);
                await context.Response.WriteAsync(json);
            }
        }
    }
}
