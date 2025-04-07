package com.mulosbron.goldbazaar.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mulosbron.goldbazaar.adapter.MarketAdapter
import com.mulosbron.goldbazaar.databinding.FragmentMarketBinding
import com.mulosbron.goldbazaar.model.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.DailyGoldPrice
import com.mulosbron.goldbazaar.service.ApiService
import io.reactivex.disposables.CompositeDisposable

class MarketFragment : Fragment(), MarketAdapter.Listener {

    private var _binding: FragmentMarketBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: ApiService
    private var goldPrices: Map<String, DailyGoldPrice> = emptyMap()
    private var dailyPercentages: Map<String, DailyGoldPercentage> = emptyMap()
    private var compositeDisposable: CompositeDisposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentMarketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiService(this)
        compositeDisposable = CompositeDisposable()

        binding.rvMarket.layoutManager = LinearLayoutManager(context)

        fetchMarketData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable?.clear()
        _binding = null
    }

    override fun onItemClick(goldType: String, goldPrice: DailyGoldPrice) {
        Toast.makeText(
            requireContext(), "Clicked: $goldType - Buying: ${goldPrice.buyingPrice}, " +
                    "Selling: ${goldPrice.sellingPrice}", Toast.LENGTH_LONG
        ).show()
    }

    private fun fetchMarketData() {
        apiService.fetchDailyPrices(compositeDisposable!!) { newGoldPrices ->
            this.goldPrices = newGoldPrices
            setAdapter()
        }

        apiService.fetchDailyPercentages(compositeDisposable!!) { newDailyPercentages ->
            this.dailyPercentages = newDailyPercentages
            setAdapter()
        }
    }

    private fun setAdapter() {
        if (goldPrices.isNotEmpty() && dailyPercentages.isNotEmpty()) {
            binding.rvMarket.adapter = MarketAdapter(goldPrices, dailyPercentages, this)
        }
    }
}
