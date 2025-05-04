package com.mulosbron.goldbazaar.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.model.entity.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.entity.DailyGoldPrice
import java.text.NumberFormat
import java.util.Locale

class MarketAdapter(
    private var goldPricesMap: Map<String, DailyGoldPrice>,
    private var dailyPercentagesMap: Map<String, DailyGoldPercentage>,
    private val listener: Listener
) : RecyclerView.Adapter<MarketAdapter.GoldViewHolder>() {

    private var goldPricesList = goldPricesMap.toList()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))

    interface Listener {
        fun onItemClick(goldType: String, goldPrice: DailyGoldPrice)
        fun onDetailButtonClick(goldType: String, goldPrice: DailyGoldPrice)
    }

    class GoldViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.text_name)
        val textBuyingPrice: TextView = view.findViewById(R.id.tvBuyingPrice)
        val textSellingPrice: TextView = view.findViewById(R.id.tvSellingPrice)
        val textBuyingPercentage: TextView = view.findViewById(R.id.tvBuyingPercentage)
        val textSellingPercentage: TextView = view.findViewById(R.id.tvSellingPercentage)
        // Removed last updated and detail button references
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoldViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_gold_price, parent, false)
        return GoldViewHolder(view)
    }

    override fun getItemCount(): Int = goldPricesList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GoldViewHolder, position: Int) {
        val (goldType, goldPrice) = goldPricesList[position]
        val dailyPercentage = dailyPercentagesMap[goldType] ?: return

        // Set gold name
        holder.textName.text = goldType

        // Format and set buying price
        holder.textBuyingPrice.text = formatCurrency(goldPrice.buyingPrice)

        // Format and set selling price
        holder.textSellingPrice.text = formatCurrency(goldPrice.sellingPrice)

        // Set buying percentage with proper styling
        val buyingPercentage = dailyPercentage.buyingPrice
        val buyingPercentageText = if (buyingPercentage > 0) "+%.2f%%" else "%.2f%%"
        holder.textBuyingPercentage.text = String.format(buyingPercentageText, buyingPercentage)
        holder.textBuyingPercentage.background = when {
            buyingPercentage > 0 -> ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.bg_percentage_positive
            )

            buyingPercentage < 0 -> ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.bg_percentage_negative
            )

            else -> ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.bg_percentage_neutral
            )
        }

        // Set selling percentage with proper styling
        val sellingPercentage = dailyPercentage.sellingPrice
        val sellingPercentageText = if (sellingPercentage > 0) "+%.2f%%" else "%.2f%%"
        holder.textSellingPercentage.text = String.format(sellingPercentageText, sellingPercentage)
        holder.textSellingPercentage.background = when {
            sellingPercentage > 0 -> ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.bg_percentage_positive
            )

            sellingPercentage < 0 -> ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.bg_percentage_negative
            )

            else -> ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.bg_percentage_neutral
            )
        }

        // Set up click listener for the entire item
        holder.itemView.setOnClickListener {
            listener.onItemClick(goldType, goldPrice)
        }

        // Removed last updated text and detail button setup
    }

    /**
     * Update the adapter data with new values
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(
        newGoldPrices: Map<String, DailyGoldPrice>,
        newDailyPercentages: Map<String, DailyGoldPercentage>
    ) {
        goldPricesMap = newGoldPrices
        dailyPercentagesMap = newDailyPercentages
        // Convert maps to list for adapter
        goldPricesList = goldPricesMap.toList()
        notifyDataSetChanged()
    }

    /**
     * Format currency values according to Turkish format
     */
    private fun formatCurrency(value: Int?): String {
        return if (value != null) {
            currencyFormat.format(value).replace("\u00A0", " ")
        } else {
            "-"
        }
    }
}