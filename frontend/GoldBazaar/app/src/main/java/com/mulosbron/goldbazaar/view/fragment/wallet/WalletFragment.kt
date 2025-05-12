package com.mulosbron.goldbazaar.view.fragment.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mulosbron.goldbazaar.databinding.FragmentWalletBinding
import com.mulosbron.goldbazaar.model.entity.Asset
import com.mulosbron.goldbazaar.view.adapter.AssetAdapter

class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val assets = listOf(
            Asset("22 Ayar Altın", 1500.0, 50.0),
            Asset("24 Ayar Altın", 1600.0, -20.0)
        )

        binding.rvPortfolio.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AssetAdapter(assets)
        }

        binding.btnAddTransaction.setOnClickListener {
            // Implement dialog to add transaction
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}