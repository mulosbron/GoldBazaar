package com.mulosbron.goldbazaar.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.model.entity.Asset

class AssetAdapter(
    private var assets: List<Asset>,
    private val getAmount: (String) -> Double,
    private val getCurrentPrice: (String) -> Double,
    private val onAssetClick: ((Asset) -> Unit)? = null
) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGoldName: TextView = itemView.findViewById(R.id.tvGoldName)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvAvgBuyPrice: TextView = itemView.findViewById(R.id.tvAvgBuyPrice)
        val tvCurrentPrice: TextView = itemView.findViewById(R.id.tvCurrentPrice)
        val tvProfitLoss: TextView = itemView.findViewById(R.id.tvProfitLoss)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assets, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets[position]
        val amount = getAmount(asset.goldName)
        val currentPrice = getCurrentPrice(asset.goldName)

        holder.tvGoldName.text = asset.goldName
        holder.tvAmount.text = String.format("%.2f", amount)
        holder.tvAvgBuyPrice.text = String.format("₺%.2f", asset.avgBuyPrice)
        holder.tvCurrentPrice.text = String.format("₺%.2f", currentPrice)

        val profit = asset.profitLoss
        holder.tvProfitLoss.text = String.format("%+.2f", profit)
        holder.tvProfitLoss.setTextColor(
            holder.itemView.context.getColor(
                when {
                    profit > 0 -> R.color.success
                    profit < 0 -> R.color.error
                    else -> R.color.price_neutral
                }
            )
        )

        holder.itemView.setOnClickListener {
            onAssetClick?.invoke(asset)
        }
    }

    override fun getItemCount(): Int = assets.size

    fun updateAssets(newAssets: List<Asset>) {
        assets = newAssets
        notifyDataSetChanged()
    }
}