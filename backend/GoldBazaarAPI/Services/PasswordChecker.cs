using System.Text.RegularExpressions;

namespace GoldBazaarAPI.Services
{
    public class PasswordChecker
    {
        public static bool IsValidPassword(string password)
        {
            if (password == null || password.Length < 8)
                return false;

            if (!Regex.IsMatch(password, "[A-Z]"))
                return false;

            if (Regex.Matches(password, @"\d").Count < 2)
                return false;

            if (!Regex.IsMatch(password, @"[!@#$%^&*()_+=\-]"))
                return false;

            return true;
        }
    }
}
