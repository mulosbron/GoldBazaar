using Microsoft.AspNetCore.Mvc;
using MongoDB.Driver;
using GoldBazaarAPI.Models;
using GoldBazaarAPI.Services;
using Org.BouncyCastle.Crypto.Generators;
using BCrypt.Net;
using GoldBazaarAPI.Data;

namespace GoldBazaarAPI.Controllers
{
    [ApiController]
    [Route("api/users")]
    public class UserController(MongoDBContext context, EmailService emailService, AuthService authService) : ControllerBase
    {
        private readonly IMongoCollection<User> _users = context.Users;
        private readonly EmailService _emailService = emailService;
        private readonly AuthService _authService = authService;

        [HttpPost("register")]
        public IActionResult Register([FromBody] AuthModel model)
        {
            if (!EmailChecker.IsValidEmail(model.Email))
            {
                return BadRequest(new { Message = "Invalid email address." });
            }

            if (!PasswordChecker.IsValidPassword(model.Password))
            {
                return BadRequest(new { Message = "Password does not meet security criteria. Must be at least 8 characters long, include at least one uppercase letter, at least two numbers, and at least one symbol." });
            }

            var existingUser = _users.Find(u => u.Email == model.Email).FirstOrDefault();
            if (existingUser != null)
            {
                return BadRequest(new { Message = "An account with this email already exists." });
            }

            var userCreationService = new UserCreation();
            var user = userCreationService.CreateUniqueUser(_users, model.Email, BCrypt.Net.BCrypt.HashPassword(model.Password));
            _users.InsertOne(user);

            return Ok(new { Message = "User successfully registered." });
        }

        [HttpPost("login")]
        public IActionResult Login([FromBody] AuthModel model)
        {
            var user = _users.Find(u => u.Email == model.Email).FirstOrDefault();
            if (user == null || !BCrypt.Net.BCrypt.Verify(model.Password, user.PasswordHash))
            {
                return Unauthorized(new { Message = "Incorrect email or password." });
            }
            if (user.BannedUntil.HasValue && user.BannedUntil > DateTime.UtcNow)
            {
                var remainingBanDuration = user.BannedUntil.Value - DateTime.UtcNow;
                return Unauthorized(new { Message = $"You are banned! Remaining ban duration: {remainingBanDuration.Days} days, {remainingBanDuration.Hours} hours and {remainingBanDuration.Minutes} minutes." });
            }

            var token = _authService.GenerateJwtToken(user.Username, user.Role);

            return Ok(new
            {
                Message = $"Welcome {user.Username}",
                Token = token
            });
        }

        [HttpPost("update-username")]
        public IActionResult UpdateUsername([FromBody] UpdateUsernameModel model)
        {
            var user = _users.Find(u => u.Email == model.Email).FirstOrDefault();
            if (user == null || !BCrypt.Net.BCrypt.Verify(model.Password, user.PasswordHash))
            {
                return Unauthorized(new { Message = "Incorrect email or password." });
            }

            var existingUserWithNewUsername = _users.Find(u => u.Username == model.NewUsername).FirstOrDefault();
            if (existingUserWithNewUsername != null)
            {
                return BadRequest(new { Message = "This username is already in use. Please choose another username." });
            }

            var update = Builders<User>.Update.Set(u => u.Username, model.NewUsername);
            _users.UpdateOne(u => u.Email == model.Email, update);

            return Ok(new { Message = "Username successfully updated." });
        }

        [HttpPost("update-password")]
        public IActionResult UpdatePassword([FromBody] UpdatePasswordModel model)
        {
            var user = _users.Find(u => u.Email == model.Email).FirstOrDefault();

            if (user == null || !BCrypt.Net.BCrypt.Verify(model.CurrentPassword, user.PasswordHash))
            {
                return Unauthorized(new { Message = "Incorrect email or current password." });
            }

            if (!PasswordChecker.IsValidPassword(model.NewPassword))
            {
                return BadRequest(new { Message = "New password does not meet security criteria. Must be at least 8 characters long, include at least one uppercase letter, at least two numbers, and at least one symbol." });
            }

            var update = Builders<User>.Update.Set(u => u.PasswordHash, BCrypt.Net.BCrypt.HashPassword(model.NewPassword));
            _users.UpdateOne(u => u.Email == model.Email, update);

            return Ok(new { Message = "Password successfully updated." });
        }

        [HttpPost("forgot-password")]
        public IActionResult ForgotPassword([FromBody] ForgotPasswordModel model)
        {
            var user = _users.Find(u => u.Email == model.Email).FirstOrDefault();
            if (user == null)
            {
                return Ok(new { Message = "Password reset instructions have been sent to your email address, if it is registered in the system." });
            }

            user.ResetToken = Guid.NewGuid().ToString();
            user.ResetTokenExpires = DateTime.UtcNow.AddHours(24);

            var update = Builders<User>.Update
                .Set(u => u.ResetToken, user.ResetToken)
                .Set(u => u.ResetTokenExpires, user.ResetTokenExpires);
            _users.UpdateOne(u => u.Email == model.Email, update);

            _emailService.SendPasswordResetEmail(user.Email, user.ResetToken);

            return Ok(new { Message = "Password reset instructions have been sent to your email address, if it is registered in the system." });
        }

        [HttpPost("reset-password")]
        public IActionResult ResetPassword([FromBody] ResetPasswordModel model)
        {
            var user = _users.Find(u => u.ResetToken == model.Token && u.ResetTokenExpires > DateTime.UtcNow).FirstOrDefault();
            if (user == null)
            {
                return BadRequest(new { Message = "Invalid or expired token." });
            }

            if (!PasswordChecker.IsValidPassword(model.NewPassword))
            {
                return BadRequest(new { Message = "New password does not meet security criteria." });
            }

            var update = Builders<User>.Update
                .Set(u => u.PasswordHash, BCrypt.Net.BCrypt.HashPassword(model.NewPassword))
                .Unset(u => u.ResetToken)
                .Unset(u => u.ResetTokenExpires);
            _users.UpdateOne(u => u.Id == user.Id, update);

            return Ok(new { Message = "Your password has been successfully reset." });
        }
    }
}