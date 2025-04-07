package com.mulosbron.goldbazaar.view

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.databinding.ActivityMainBinding
import com.mulosbron.goldbazaar.view.fragment.MarketFragment
import com.mulosbron.goldbazaar.view.fragment.WalletFragment
import com.mulosbron.goldbazaar.view.fragment.NewsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(MarketFragment())

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_markets -> {
                    replaceFragment(MarketFragment())
                    true
                }

                R.id.navigation_portfolio, R.id.navigation_settings -> {
                    replaceFragment(if (item.itemId == R.id.navigation_portfolio) WalletFragment() else NewsFragment())
                    true
                    /*
                    if (checkUserLoggedIn()) {
                        replaceFragment(if (item.itemId == R.id.navigation_portfolio) WalletFragment() else NewsFragment())
                        true
                    } else {
                        replaceFragment(LoginFragment())
                        true
                    }
                     */
                }

                else -> false
            }
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()
    }
}