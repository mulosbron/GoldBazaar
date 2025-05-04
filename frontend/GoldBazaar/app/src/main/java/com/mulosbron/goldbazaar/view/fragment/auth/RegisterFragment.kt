package com.mulosbron.goldbazaar.view.fragment.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.databinding.FragmentRegisterBinding
import com.mulosbron.goldbazaar.util.ext.navigateToLoginFromRegister
import com.mulosbron.goldbazaar.viewmodel.auth.AuthUiState
import com.mulosbron.goldbazaar.viewmodel.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    // Input validation flags
    private var isEmailValid = false
    private var isPasswordValid = false
    private var isPasswordMatching = false
    private var isTermsAccepted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
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
        // Set initial button state
        updateButtonState()
    }

    private fun setupInputValidation() {
        // Email validation
        binding.etEmail.addTextChangedListener { text ->
            val email = text.toString().trim()
            isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

            binding.emailInputLayout.error = if (email.isNotEmpty() && !isEmailValid) {
                getString(R.string.invalid_email)
            } else {
                null
            }

            updateButtonState()
        }

        // Password validation
        binding.etPassword.addTextChangedListener { text ->
            val password = text.toString().trim()
            isPasswordValid = password.length >= 6

            binding.passwordInputLayout.error = if (password.isNotEmpty() && !isPasswordValid) {
                getString(R.string.password_length_error)
            } else {
                null
            }

            // Check if passwords match
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            if (confirmPassword.isNotEmpty()) {
                isPasswordMatching = password == confirmPassword

                binding.confirmPasswordInputLayout.error = if (!isPasswordMatching) {
                    getString(R.string.passwords_not_match)
                } else {
                    null
                }
            }

            updateButtonState()
        }

        // Confirm password validation
        binding.etConfirmPassword.addTextChangedListener { text ->
            val confirmPassword = text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (confirmPassword.isNotEmpty() && password.isNotEmpty()) {
                isPasswordMatching = password == confirmPassword

                binding.confirmPasswordInputLayout.error = if (!isPasswordMatching) {
                    getString(R.string.passwords_not_match)
                } else {
                    null
                }

                updateButtonState()
            }
        }

        // Terms checkbox
        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            isTermsAccepted = isChecked
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        binding.btnSignUp.isEnabled = isEmailValid && isPasswordValid &&
                isPasswordMatching && isTermsAccepted
        binding.btnSignUp.alpha = if (binding.btnSignUp.isEnabled) 1.0f else 0.6f
    }

    private fun observeViewModel() {
        authViewModel.uiState.observe(/* owner = */ viewLifecycleOwner)  /* observer = */ { state ->
            when (state) {
                is AuthUiState.Idle -> {
                    // İlk durum, bir şey yapmaya gerek yok
                }

                is AuthUiState.Loading -> {
                    showLoading(true)
                }

                is AuthUiState.RegisterSuccess -> {
                    showLoading(false)
                    showSuccessMessage(state.message)
                    // Navigate to login screen
                    navigateToLoginFromRegister()
                }

                is AuthUiState.Error -> {
                    showLoading(false)
                    showErrorMessage(state.message)
                }

                else -> {
                    // Diğer durumlar bu fragment için gerekli değil
                    showLoading(false)
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Register button
        binding.btnSignUp.setOnClickListener {
            performRegister()
        }

        // Login text button
        binding.tvLogin.setOnClickListener {
            navigateToLoginFromRegister()
        }

        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun performRegister() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorMessage(getString(R.string.fill_all_fields))
            return
        }

        if (password != confirmPassword) {
            showErrorMessage(getString(R.string.passwords_not_match))
            return
        }

        if (!binding.cbTerms.isChecked) {
            showErrorMessage("Devam etmek için kullanım koşullarını kabul etmelisiniz")
            return
        }

        authViewModel.registerUser(email, password)
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

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSignUp.isEnabled = !show

        // Disable inputs during loading
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
        binding.etConfirmPassword.isEnabled = !show
        binding.cbTerms.isEnabled = !show
        binding.btnBack.isEnabled = !show
        binding.tvLogin.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}