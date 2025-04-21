using System.Net;
using System.Net.Mail;
using Microsoft.Extensions.Configuration;

namespace GoldBazaarAPI.Services
{
    public class EmailService
    {
        private readonly SmtpClient _smtpClient;
        private readonly string _fromEmail;

        public EmailService(IConfiguration configuration)
        {
            var smtpServer = configuration["EmailSettings:SMTPServer"] ?? throw new InvalidOperationException("SMTP server configuration is missing.");
            var smtpPortString = configuration["EmailSettings:SMTPPort"];
            var smtpPort = smtpPortString != null ? int.Parse(smtpPortString) : throw new InvalidOperationException("SMTP port configuration is missing.");
            var smtpUsername = configuration["EmailSettings:SMTPUsername"] ?? throw new InvalidOperationException("SMTP username configuration is missing.");
            var smtpPassword = configuration["EmailSettings:SMTPPassword"] ?? throw new InvalidOperationException("SMTP password configuration is missing.");
            _fromEmail = smtpUsername;

            _smtpClient = new SmtpClient(smtpServer)
            {
                Port = smtpPort,
                Credentials = new NetworkCredential(smtpUsername, smtpPassword),
                EnableSsl = true,
            };
        }

        public void SendPasswordResetEmail(string toEmail, string resetToken)
        {
            using var mailMessage = new MailMessage(_fromEmail, toEmail)
            {
                Subject = "Şifre Sıfırlama",
                Body = $"Şifre sıfırlama kodunuz: {resetToken}",
                IsBodyHtml = true
            };

            _smtpClient.Send(mailMessage);
        }
    }
}
