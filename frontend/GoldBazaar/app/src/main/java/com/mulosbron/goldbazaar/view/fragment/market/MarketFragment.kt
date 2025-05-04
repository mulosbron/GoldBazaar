package com.mulosbron.goldbazaar.view.fragment.market

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.databinding.FragmentMarketBinding
import com.mulosbron.goldbazaar.model.entity.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.entity.DailyGoldPrice
import com.mulosbron.goldbazaar.view.adapter.MarketAdapter
import com.mulosbron.goldbazaar.viewmodel.market.MarketUiState
import com.mulosbron.goldbazaar.viewmodel.market.MarketViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarketFragment : Fragment(), MarketAdapter.Listener {

    private var _binding: FragmentMarketBinding? = null
    private val binding get() = _binding!!

    private val marketViewModel: MarketViewModel by viewModel()
    private var marketAdapter: MarketAdapter? = null
    private var originalGoldPrices: Map<String, DailyGoldPrice> = emptyMap()
    private var originalDailyPercentages: Map<String, DailyGoldPercentage> = emptyMap()
    private var filteredGoldPrices: Map<String, DailyGoldPrice> = emptyMap()
    private var filteredDailyPercentages: Map<String, DailyGoldPercentage> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        observeViewModel()
        fetchMarketData()
    }

    private fun setupUI() {
        // Set current date to last updated time initially
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.lastUpdatedCombined.text =
            getString(R.string.last_update, dateFormat.format(Date()))

        // Setup search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterGoldItems(s.toString())
            }
        })

        // Sıralama butonu kaldırıldı

        // Setup swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        // Setup retry button
        binding.retryButton.setOnClickListener {
            fetchMarketData()
        }
    }

    private fun observeViewModel() {
        marketViewModel.marketUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MarketUiState.Loading -> {
                    showLoading(true)
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                is MarketUiState.Success -> {
                    showLoading(false)
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.emptyStateContainer.isVisible = false
                }

                is MarketUiState.Error -> {
                    showLoading(false)
                    binding.swipeRefreshLayout.isRefreshing = false
                    if (originalGoldPrices.isEmpty()) {
                        binding.emptyStateContainer.isVisible = true
                    }
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }

                else -> {
                    // İlk durum için bir şey yapmaya gerek yok
                }
            }
        }

        // Altın fiyatları güncellendiğinde adaptörü güncelleme
        marketViewModel.goldPrices.observe(viewLifecycleOwner) { goldPrices ->
            originalGoldPrices = goldPrices
            updateAdapter()
        }

        // Günlük yüzdeler güncellendiğinde adaptörü güncelleme
        marketViewModel.dailyPercentages.observe(viewLifecycleOwner) { percentages ->
            originalDailyPercentages = percentages
            updateAdapter()
        }

        // Son güncelleme zamanını gözlemleme
        marketViewModel.lastUpdated.observe(viewLifecycleOwner) { lastUpdated ->
            binding.lastUpdatedCombined.text = getString(R.string.last_update, lastUpdated)

        }
    }

    override fun onItemClick(goldType: String, goldPrice: DailyGoldPrice) {
        // Show detailed dialog with gold information
        showGoldDetailsDialog(goldType, goldPrice)
    }

    override fun onDetailButtonClick(goldType: String, goldPrice: DailyGoldPrice) {
        // Navigate to a detailed fragment or show a detailed dialog
        showGoldDetailsDialog(goldType, goldPrice)
    }

    private fun setupRecyclerView() {
        binding.rvMarket.layoutManager = LinearLayoutManager(context)
        binding.rvMarket.setHasFixedSize(true)
    }

    private fun fetchMarketData() {
        binding.emptyStateContainer.isVisible = false
        marketViewModel.fetchMarketData()
    }

    fun refreshData() {
        marketViewModel.refreshData()
    }

    private fun updateAdapter() {
        if (originalGoldPrices.isNotEmpty() && originalDailyPercentages.isNotEmpty()) {
            // Apply current filter if any
            applyCurrentFilter()

            if (marketAdapter == null) {
                marketAdapter = MarketAdapter(filteredGoldPrices, filteredDailyPercentages, this)
                binding.rvMarket.adapter = marketAdapter
            } else {
                marketAdapter?.updateData(filteredGoldPrices, filteredDailyPercentages)
            }
        }
    }

    private fun filterGoldItems(query: String) {
        if (query.isEmpty()) {
            filteredGoldPrices = originalGoldPrices
            filteredDailyPercentages = originalDailyPercentages
        } else {
            filteredGoldPrices = originalGoldPrices.filter { (key, _) ->
                key.contains(query, ignoreCase = true)
            }

            filteredDailyPercentages = originalDailyPercentages.filter { (key, _) ->
                key.contains(query, ignoreCase = true)
            }
        }

        marketAdapter?.updateData(filteredGoldPrices, filteredDailyPercentages)
    }

    private fun applyCurrentFilter() {
        // Get current filter criteria from search field
        val currentSearchText = binding.searchEditText.text.toString()
        filterGoldItems(currentSearchText)
    }

    @SuppressLint("DefaultLocale")
    private fun showGoldDetailsDialog(goldType: String, goldPrice: DailyGoldPrice) {
        val percentage = originalDailyPercentages[goldType]
        val buyingTrend = if ((percentage?.buyingPrice ?: 0.0) > 0) "artışta" else "düşüşte"

        val message = """
            ${goldPrice.getCleanName()}
            
            Alış Fiyatı: ${goldPrice.buyingPrice ?: "Bilinmiyor"}
            Satış Fiyatı: ${goldPrice.sellingPrice ?: "Bilinmiyor"}
            
            Alış Değişim: %${String.format("%.2f", percentage?.buyingPrice ?: 0.0)} ($buyingTrend)
            Satış Değişim: %${String.format("%.2f", percentage?.sellingPrice ?: 0.0)}
            
            Son Güncelleme: ${goldPrice.lastUpdated ?: "Bilinmiyor"}
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Altın Detayları")
            .setMessage(message)
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        marketAdapter = null
    }
}