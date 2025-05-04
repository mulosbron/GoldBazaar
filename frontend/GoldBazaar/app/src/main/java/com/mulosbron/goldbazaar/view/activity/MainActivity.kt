package com.mulosbron.goldbazaar.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.databinding.ActivityMainBinding
import com.mulosbron.goldbazaar.util.SharedPrefsManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefsManager = SharedPrefsManager(this)

        setupNavigation()
        setupUI()
    }

    private fun setupNavigation() {
        // NavHostFragment'ı doğru şekilde al
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Bottom Navigation'ı NavController ile bağla
        binding.bottomNavigationView.setupWithNavController(navController)

        // Giriş kontrolü için özel davranış ekle
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_markets -> {
                    navController.navigate(R.id.navigation_markets)
                    true
                }

                R.id.navigation_wallet -> {
                    if (sharedPrefsManager.isUserLoggedIn()) {
                        navController.navigate(R.id.navigation_wallet)
                    } else {
                        navController.navigate(R.id.navigation_login)
                        showSnackbar(getString(R.string.please_login_to_continue))
                    }
                    true
                }

                R.id.navigation_news -> {
                    navController.navigate(R.id.navigation_news)
                    true
                }

                else -> false
            }
        }
    }

    private fun setupUI() {
        binding.settingsButton.setOnClickListener {
            if (sharedPrefsManager.isUserLoggedIn()) {
                showLogoutDialog()
            } else {
                showSnackbar("Ayarlar için giriş yapmalısınız")
            }
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

    // Yeni navigation fonksiyonları
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
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.primary))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }
}