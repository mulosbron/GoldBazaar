package com.mulosbron.goldbazaar.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mulosbron.goldbazaar.R

class GoldTypeAdapter(private val goldTypes: List<String>) : RecyclerView.Adapter<GoldTypeAdapter.GoldTypeViewHolder>() {

    class GoldTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGoldName: TextView = itemView.findViewById(R.id.tvGoldName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoldTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_layout_add_transaction, parent, false)
        return GoldTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoldTypeViewHolder, position: Int) {
        holder.tvGoldName.text = goldTypes[position]
    }

    override fun getItemCount(): Int = goldTypes.size
}