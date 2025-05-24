package com.mulosbron.goldbazaar.view.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.databinding.ActivityMainBinding
import com.mulosbron.goldbazaar.util.SharedPrefsManager
import com.mulosbron.goldbazaar.util.ThemeManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getCurrentThemeRes(this)) // 🔥 Tüm tema olayı burada!
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefsManager = SharedPrefsManager(this)

        setupNavigation()
        setupUI()
    }

    private fun setupNavigation() {
        // NavHostFragment'ı al ve navController'a ata
        val host = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController

        // BottomNavigationView ile navController'ı bağla
        binding.bottomNavigationView.setupWithNavController(navController)

        // Bottom nav seçimine göre fragment geçişleri
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_markets -> {
                    navController.navigate(R.id.navigation_markets)
                    true
                }
                R.id.navigation_wallet -> {
                    // Always navigate to wallet, no login check
                    navController.navigate(R.id.navigation_wallet)
                    true
                }
//                // Eğer login kontrolü istiyorsan bu bloğu kullan:
//                R.id.navigation_wallet -> {
//                    if (sharedPrefsManager.isUserLoggedIn()) {
//                        navController.navigate(R.id.navigation_wallet)
//                    } else {
//                        navController.navigate(R.id.navigation_login)
//                        showSnackbar(getString(R.string.please_login_to_continue))
//                    }
//                    true
//                }
                R.id.navigation_news -> {
                    navController.navigate(R.id.navigation_news)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupUI() {
        // Top bar'daki ayar butonunu dinle
        binding.settingsButton.setOnClickListener {
            navController.navigate(R.id.navigation_settings)
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hesaptan Çıkış")
            .setMessage("Hesabınızdan çıkış yapmak istediğinize emin misiniz?")
            .setPositiveButton("Evet") { _, _ -> logOutUser() }
            .setNegativeButton("Hayır", null)
            .show()
    }

    // Yeni navigation fonksiyonları, Login/Register sonrası çağırılır
    fun navigateToWalletAfterLogin() {
        // BottomNavigationView'ı güncelle
        binding.bottomNavigationView.selectedItemId = R.id.navigation_wallet
        showSnackbar(getString(R.string.welcome_back, getUsername()))
    }

    fun saveAuthToken(token: String) {
        sharedPrefsManager.saveAuthToken(token)
    }

    fun saveUsername(username: String) {
        sharedPrefsManager.saveUsername(username)
    }

    fun getUsername(): String {
        return sharedPrefsManager.getUsername()
    }

    fun logOutUser() {
        sharedPrefsManager.clearUserData()
        navController.navigate(R.id.navigation_login)
        binding.bottomNavigationView.selectedItemId = R.id.navigation_markets
        showSnackbar(getString(R.string.logged_out_successfully))
    }

    private fun showSnackbar(message: String) {
        // Tema renkleriyle Snackbar gösterimi
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
        val colorPrimary = typedValue.data
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
        val colorOnPrimary = typedValue.data

        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(colorPrimary)
            .setTextColor(colorOnPrimary)
            .show()
    }
}
