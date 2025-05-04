package com.mulosbron.goldbazaar.view.fragment.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.databinding.FragmentLoginBinding
import com.mulosbron.goldbazaar.util.ext.navigateToForgotPasswordFromLogin
import com.mulosbron.goldbazaar.util.ext.navigateToRegisterFromLogin
import com.mulosbron.goldbazaar.util.ext.saveAuthData
import com.mulosbron.goldbazaar.view.activity.MainActivity
import com.mulosbron.goldbazaar.viewmodel.auth.AuthUiState
import com.mulosbron.goldbazaar.viewmodel.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    // Input validation flags
    private var isEmailValid = false
    private var isPasswordValid = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupInputValidation()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Set up initial button state
        updateLoginButtonState()
    }

    private fun setupInputValidation() {
        // Email validation
        binding.etEmail.addTextChangedListener {
            val email = it.toString().trim()
            isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

            binding.emailInputLayout.error = if (email.isNotEmpty() && !isEmailValid) {
                getString(R.string.invalid_email)
            } else {
                null
            }

            updateLoginButtonState()
        }

        // Password validation
        binding.etPassword.addTextChangedListener {
            val password = it.toString().trim()
            isPasswordValid = password.length >= 6

            binding.passwordInputLayout.error = if (password.isNotEmpty() && !isPasswordValid) {
                getString(R.string.password_length_error)
            } else {
                null
            }

            updateLoginButtonState()
        }
    }

    private fun updateLoginButtonState() {
        binding.btnLogin.isEnabled = isEmailValid && isPasswordValid
        binding.btnLogin.alpha = if (binding.btnLogin.isEnabled) 1.0f else 0.6f
    }

    private fun observeViewModel() {
        authViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthUiState.Idle -> {
                    // İlk durum, bir şey yapmaya gerek yok
                }

                is AuthUiState.Loading -> {
                    showLoading(true)
                }

                is AuthUiState.LoginSuccess -> {
                    showLoading(false)
                    // Auth bilgilerini kaydet
                    saveAuthData(state.token, state.username)

                    // MainActivity'deki navigateToWalletAfterLogin fonksiyonunu kullan
                    (activity as? MainActivity)?.navigateToWalletAfterLogin()

                    showSuccessMessage("${state.username} hoşgeldiniz!")
                }

                is AuthUiState.Error -> {
                    showLoading(false)
                    showErrorMessage(state.message)
                }

                else -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.tvForgotPassword.setOnClickListener {
            // Navigation Component action'ını kullan
            navigateToForgotPasswordFromLogin()
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            // Navigation Component action'ını kullan
            navigateToRegisterFromLogin()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val rememberMe = binding.cbRememberMe.isChecked

        authViewModel.loginUser(email, password, rememberMe)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show

        // Disable inputs during loading
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
        binding.tvForgotPassword.isEnabled = !show
        binding.tvRegister.isEnabled = !show
        binding.cbRememberMe.isEnabled = !show
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .setTextColor(resources.getColor(R.color.white, null))
            .show()
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .setTextColor(resources.getColor(R.color.white, null))
            .setAction("Tamam") {
                // Dismiss action
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}