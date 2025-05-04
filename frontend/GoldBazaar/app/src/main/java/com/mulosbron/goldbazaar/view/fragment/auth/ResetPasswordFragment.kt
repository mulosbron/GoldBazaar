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
import com.mulosbron.goldbazaar.databinding.FragmentResetPasswordBinding
import com.mulosbron.goldbazaar.viewmodel.auth.AuthUiState
import com.mulosbron.goldbazaar.viewmodel.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    // Input validation flags
    private var isTokenValid = false
    private var isPasswordValid = false
    private var isPasswordMatching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
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
        // Token validation
        binding.etToken.addTextChangedListener { text ->
            val token = text.toString().trim()
            isTokenValid = token.isNotEmpty()

            binding.tokenInputLayout.error = if (token.isEmpty()) {
                "Token alanı boş olamaz"
            } else {
                null
            }

            updateButtonState()
        }

        // Password validation
        binding.etNewPassword.addTextChangedListener { text ->
            val password = text.toString().trim()
            isPasswordValid = password.length >= 6

            binding.newPasswordInputLayout.error = if (password.isNotEmpty() && !isPasswordValid) {
                getString(R.string.password_length_error)
            } else {
                null
            }

            // Check if passwords match
            val confirmPassword = binding.etConfirmNewPassword.text.toString().trim()
            if (confirmPassword.isNotEmpty()) {
                isPasswordMatching = password == confirmPassword

                binding.confirmNewPasswordInputLayout.error = if (!isPasswordMatching) {
                    getString(R.string.passwords_not_match)
                } else {
                    null
                }
            }

            updateButtonState()
        }

        // Confirm password validation
        binding.etConfirmNewPassword.addTextChangedListener { text ->
            val confirmPassword = text.toString().trim()
            val password = binding.etNewPassword.text.toString().trim()

            if (confirmPassword.isNotEmpty() && password.isNotEmpty()) {
                isPasswordMatching = password == confirmPassword

                binding.confirmNewPasswordInputLayout.error = if (!isPasswordMatching) {
                    getString(R.string.passwords_not_match)
                } else {
                    null
                }

                updateButtonState()
            }
        }
    }

    private fun updateButtonState() {
        binding.btnResetPassword.isEnabled = isTokenValid && isPasswordValid && isPasswordMatching
        binding.btnResetPassword.alpha = if (binding.btnResetPassword.isEnabled) 1.0f else 0.6f
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

                is AuthUiState.ResetPasswordSuccess -> {
                    showLoading(false)
                    showSuccessMessage(state.message)
                    // Navigate to login screen
                    findNavController().navigate(R.id.action_reset_password_to_login)
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
        binding.btnResetPassword.setOnClickListener {
            performResetPassword()
        }

        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun performResetPassword() {
        val token = binding.etToken.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmNewPassword = binding.etConfirmNewPassword.text.toString().trim()

        if (token.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            showErrorMessage(getString(R.string.fill_all_fields))
            return
        }

        if (newPassword != confirmNewPassword) {
            showErrorMessage(getString(R.string.passwords_not_match))
            return
        }

        authViewModel.resetPassword(token, newPassword)
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
        binding.btnResetPassword.isEnabled = !show

        // Disable inputs during loading
        binding.etToken.isEnabled = !show
        binding.etNewPassword.isEnabled = !show
        binding.etConfirmNewPassword.isEnabled = !show
        binding.btnBack.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}