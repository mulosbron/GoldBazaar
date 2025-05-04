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
import com.mulosbron.goldbazaar.databinding.FragmentForgotPasswordBinding
import com.mulosbron.goldbazaar.util.ext.navigateToLoginFromForgotPassword
import com.mulosbron.goldbazaar.viewmodel.auth.AuthUiState
import com.mulosbron.goldbazaar.viewmodel.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModel()

    private var isEmailValid = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
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
        binding.etEmailForReset.addTextChangedListener { text ->
            val email = text.toString().trim()
            isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

            // Set error message if needed
            binding.emailResetInputLayout.error = if (email.isNotEmpty() && !isEmailValid) {
                getString(R.string.invalid_email)
            } else {
                null
            }

            updateButtonState()
        }
    }

    private fun updateButtonState() {
        binding.btnForgotPassword.isEnabled = isEmailValid
        binding.btnForgotPassword.alpha = if (binding.btnForgotPassword.isEnabled) 1.0f else 0.6f
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

                is AuthUiState.ForgotPasswordSuccess -> {
                    showLoading(false)
                    showSuccessMessage(state.message)

                    // Navigation Component ile ResetPasswordFragment'a git
                    // Safe Args kullanmıyorsanız bu şekilde navigasyon yapın
                    findNavController().navigate(R.id.action_forgot_password_to_reset_password)
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
        // Reset password button
        binding.btnForgotPassword.setOnClickListener {
            performForgotPassword()
        }

        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Return to login button
        binding.btnBackToLogin.setOnClickListener {
            navigateToLoginFromForgotPassword()
        }
    }

    private fun performForgotPassword() {
        val email = binding.etEmailForReset.text.toString().trim()

        if (email.isEmpty()) {
            showErrorMessage(getString(R.string.fill_all_fields))
            return
        }

        authViewModel.forgotPassword(email)
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
        binding.btnForgotPassword.isEnabled = !show

        // Disable inputs during loading
        binding.etEmailForReset.isEnabled = !show
        binding.btnBack.isEnabled = !show
        binding.btnBackToLogin.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}