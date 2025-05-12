package com.mulosbron.goldbazaar.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.model.entity.Asset

class AssetAdapter(private val assets: List<Asset>) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGoldName: TextView = itemView.findViewById(R.id.tvGoldName)
        val tvAvgBuyPrice: TextView = itemView.findViewById(R.id.tvAvgBuyPrice)
        val tvProfitLoss: TextView = itemView.findViewById(R.id.tvProfitLoss)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_layout_portfolio, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets[position]
        holder.tvGoldName.text = asset.goldName
        holder.tvAvgBuyPrice.text = asset.avgBuyPrice.toString()
        holder.tvProfitLoss.text = asset.profitLoss.toString()
    }

    override fun getItemCount(): Int = assets.size
}