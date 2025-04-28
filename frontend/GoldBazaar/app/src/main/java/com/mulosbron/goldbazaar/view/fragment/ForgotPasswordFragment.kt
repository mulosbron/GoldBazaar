package com.mulosbron.goldbazaar.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mulosbron.goldbazaar.databinding.FragmentForgotPasswordBinding
import com.mulosbron.goldbazaar.service.ApiService

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService

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

        apiService = ApiService(this)

        binding.btnForgotPassword.setOnClickListener {
            performForgotPassword()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun performForgotPassword() {
        val email = binding.etEmailForReset.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.forgotPassword(email)
    }
}