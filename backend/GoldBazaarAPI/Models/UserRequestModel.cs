namespace GoldBazaarAPI.Models
{
    public class AuthModel
    {
        public required string Email { get; set; }
        public required string Password { get; set; }
    }

    public class UpdateUsernameModel
    {
        public required string Email { get; set; }
        public required string Password { get; set; }
        public required string NewUsername { get; set; }
    }

    public class UpdatePasswordModel
    {
        public required string Email { get; set; }
        public required string CurrentPassword { get; set; }
        public required string NewPassword { get; set; }
    }

    public class ForgotPasswordModel
    {
        public required string Email { get; set; }
    }

    public class ResetPasswordModel
    {
        public required string Token { get; set; }
        public required string NewPassword { get; set; }
    }
}