package com.mulosbron.goldbazaar.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.model.entity.Transaction

class TransactionAdapter(private val transactions: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTransactionType: TextView = itemView.findViewById(R.id.tvTransactionType)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val btnEditTransaction: Button = itemView.findViewById(R.id.btnEditTransaction)
        val btnDeleteTransaction: Button = itemView.findViewById(R.id.btnDeleteTransaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_layout_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvTransactionType.text = transaction.transactionType // Değişiklik burada yapıldı
        holder.tvAmount.text = transaction.amount.toString()
        holder.tvDate.text = transaction.date
        holder.tvPrice.text = transaction.price.toString()
        holder.btnEditTransaction.setOnClickListener { /* Implement edit logic */ }
        holder.btnDeleteTransaction.setOnClickListener { /* Implement delete logic */ }
    }

    override fun getItemCount(): Int = transactions.size
}