package com.mulosbron.goldbazaar.view.fragment.wallet

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.databinding.FragmentWalletBinding
import com.mulosbron.goldbazaar.model.entity.*
import com.mulosbron.goldbazaar.view.adapter.AssetAdapter
import com.mulosbron.goldbazaar.viewmodel.wallet.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class WalletFragment : Fragment() {

    // ───────── UI & VM ─────────
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WalletViewModel by viewModel()

    private lateinit var assetAdapter: AssetAdapter
    private lateinit var pieChart: PieChart

    // ───────── Fragment life-cycle ─────────
    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentWalletBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        setupRecycler()
        setupPieChart()
        setupObservers()
        binding.btnAddTransaction.setOnClickListener { showAddTransactionDialog() }
        viewModel.loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView(); _binding = null
    }

    // ───────── RecyclerView ─────────
    private fun setupRecycler() {
        binding.rvPortfolio.layoutManager = LinearLayoutManager(context)
        assetAdapter = AssetAdapter(
            emptyList(),
            getAmount = { name ->
                viewModel.transactions.value
                    ?.filter { it.asset == name && it.transactionType == "buy" }
                    ?.sumOf { it.amount } ?: 0.0
            },
            getCurrentPrice = { name -> viewModel.getCurrentPrice(name, false) },
            onAssetClick = { asset ->  // liste öğesine tıkla
                showAssetTransactionsDialog(asset.goldName)
            }
        )
        binding.rvPortfolio.adapter = assetAdapter
    }

    // ───────── LiveData observers ─────────
    private fun setupObservers() {

        /* 4 farklı LiveData değiştiğinde aynı hesaplamayı yapıyoruz */
        val refresh: () -> Unit = { refreshAssetList() }

        viewModel.assets.observe(viewLifecycleOwner)        { refresh() }
        viewModel.transactions.observe(viewLifecycleOwner)  { refresh() }
        viewModel.profits.observe(viewLifecycleOwner)       { refresh() }
        viewModel.averagePrices.observe(viewLifecycleOwner) { refresh() }

        viewModel.uiState.observe(viewLifecycleOwner) {
            binding.progressBar.visibility =
                if (it is WalletUiState.Loading) View.VISIBLE else View.GONE

            if (it is WalletUiState.Error)
                showError(it.message)
        }
    }

    private fun refreshAssetList() {
        val names  = viewModel.assets.value ?: emptyList()
        val assets = names.map { n ->
            Asset(
                goldName    = n,
                avgBuyPrice = viewModel.averagePrices.value?.get(n) ?: 0.0,
                profitLoss  = viewModel.profits.value?.get(n) ?: 0.0
            )
        }
        assetAdapter.updateAssets(assets)
        updatePieChart(assets)
        updateWalletSummary()
    }

    // ───────── Pie chart ─────────
    private fun setupPieChart() = binding.pieChart.apply {
        description.isEnabled = false
        isDrawHoleEnabled     = false
        setUsePercentValues(false)
        setDrawEntryLabels(false)
        legend.isEnabled = true
        setEntryLabelColor(Color.TRANSPARENT)
        pieChart = this
    }

    private fun updatePieChart(list: List<Asset>) {
        val total = viewModel.calculateTotalPortfolioValue()
        val entries = list.mapNotNull { a ->
            val amount = viewModel.transactions.value
                ?.filter { it.asset == a.goldName && it.transactionType == "buy" }
                ?.sumOf { it.amount } ?: 0.0
            val value  = a.avgBuyPrice * amount
            if (value > 0) PieEntry(value.toFloat(), a.goldName) else null
        }

        pieChart.data = PieData(PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = Color.TRANSPARENT
        })
        pieChart.invalidate()
    }

    // ───────── FAB / Dialog’lar ─────────
    private fun showAddTransactionDialog() {
        val assets = viewModel.getAvailableAssets()
        if (assets.isEmpty())   { showError("Kullanılabilir altın türü yok."); return }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Altın Türü Seçin")
            .setItems(assets.toTypedArray()) { _, idx ->
                showTransactionDetailsDialog(assets[idx])
            }.show()
    }

    private fun showTransactionDetailsDialog(asset: String) {
        val view = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
        val etAmount = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAmount)
        val etPrice  = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPrice)
        val rbBuy    = view.findViewById<android.widget.RadioButton>(R.id.rbBuy)

        etPrice.setText(viewModel.getCurrentPrice(asset, true).toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("İşlem Detayları")
            .setView(view)
            .setPositiveButton("Ekle") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull()
                val price  = etPrice.text.toString().toDoubleOrNull()
                if (amount != null && price != null) {
                    viewModel.addTransaction(
                        Transaction(
                            asset = asset,
                            transactionType = if (rbBuy.isChecked) "buy" else "sell",
                            amount = amount,
                            date = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()
                            ).format(Date()),
                            price = price
                        )
                    )
                } else showError("Lütfen geçerli değerler girin")
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    // ───────── İşlem listesi & edit/sil ─────────
    private fun showAssetTransactionsDialog(asset: String) {
        val txList = viewModel.transactions.value?.filter { it.asset == asset }.orEmpty()
        if (txList.isEmpty()) { showError("Bu varlığa ait işlem yok."); return }

        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_list, null)
        val container  = dialogView.findViewById<LinearLayout>(R.id.transactionListContainer)

        // başlık
        container.addView(TextView(requireContext()).apply {
            text = "$asset İşlemleri"
            setTextAppearance(R.style.DialogHeaderText)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        })

        // her işlem satırı
        txList.forEach { tx ->
            val tv = TextView(requireContext()).apply {
                val type = if (tx.transactionType == "buy") "Alış" else "Satış"
                text = "$type - ${tx.amount} - ${tx.price}₺\n${tx.date}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                setPadding(8, 8, 8, 8)
                setOnClickListener { showTransactionOptionsDialog(tx) }   // <<< tıkla & düzenle/sil
            }
            container.addView(tv)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setNegativeButton("Kapat", null)
            .show()
    }

    private fun showTransactionOptionsDialog(tx: Transaction) {
        val opt = arrayOf("Düzenle", "Sil")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("İşlem Seçenekleri")
            .setItems(opt) { _, idx ->
                when (idx) {
                    0 -> showUpdateTransactionDialog(tx)
                    1 -> viewModel.deleteTransaction(tx.id)
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showUpdateTransactionDialog(tx: Transaction) {
        val view = layoutInflater.inflate(R.layout.dialog_update_transaction, null)
        val etAmount = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUpdateAmount)
        val etPrice  = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUpdatePrice)
        val rbBuy    = view.findViewById<android.widget.RadioButton>(R.id.rbUpdateBuy)
        val rbSell   = view.findViewById<android.widget.RadioButton>(R.id.rbUpdateSell)

        etAmount.setText(tx.amount.toString())
        etPrice.setText(tx.price.toString())
        if (tx.transactionType == "buy") rbBuy.isChecked = true else rbSell.isChecked = true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("İşlemi Düzenle")
            .setView(view)
            .setPositiveButton("Kaydet") { _, _ ->
                val newAmt = etAmount.text.toString().toDoubleOrNull()
                val newPrc = etPrice.text.toString().toDoubleOrNull()
                val isBuy  = rbBuy.isChecked
                if (newAmt != null && newPrc != null) {
                    viewModel.updateTransaction(
                        tx.id, tx.copy(
                            amount = newAmt,
                            price  = newPrc,
                            transactionType = if (isBuy) "buy" else "sell"
                        )
                    )
                } else showError("Lütfen geçerli değerler girin")
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    // ───────── Cüzdan özetleri ─────────
    private fun updateWalletSummary() {
        val total  = viewModel.calculateTotalPortfolioValue()
        val profit = viewModel.profits.value?.values?.sum() ?: 0.0
        val pct    = if (total > 0) profit / (total - profit) * 100 else 0.0

        binding.tvTotalWalletValue.text = "₺%.2f".format(total)

        val fmt = if (profit >= 0) "+%.2f (%.2f%%)" else "%.2f (%.2f%%)"
        binding.tvTotalProfitLoss.apply {
            text = fmt.format(profit, pct)
            setTextColor(ContextCompat.getColor(
                requireContext(),
                if (profit >= 0) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            ))
        }
    }

    // ───────── Yardımcı ─────────
    private fun showError(msg: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hata")
            .setMessage(msg)
            .setPositiveButton("Tamam", null)
            .show()
    }
}
