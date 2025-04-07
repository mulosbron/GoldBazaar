package com.mulosbron.goldbazaar.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.model.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.DailyGoldPrice

class MarketAdapter(
    private val goldPricesMap: Map<String, DailyGoldPrice>,
    private val dailyPercentagesMap: Map<String, DailyGoldPercentage>,
    private val listener: Listener
) : RecyclerView.Adapter<MarketAdapter.RowHolder>() {

    private val colors: Array<String> = arrayOf("#FFD700", "#FFDF00")
    private val goldPricesList = goldPricesMap.toList()
    private val dailyPercentagesList = dailyPercentagesMap.toList()

    interface Listener {
        fun onItemClick(goldType: String, goldPrice: DailyGoldPrice)
    }

    class RowHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val textName: TextView = view.findViewById(R.id.text_name)
        private val textBuyingPrice: TextView = view.findViewById(R.id.tvBuyingPrice)
        private val textSellingPrice: TextView = view.findViewById(R.id.tvSellingPrice)
        private val textBuyingPercentage: TextView = view.findViewById(R.id.tvBuyingPercentage)
        private val textSellingPercentage: TextView = view.findViewById(R.id.tvSellingPercentage)


        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(
            goldType: String,
            goldPrice: DailyGoldPrice,
            dailyPercentage: DailyGoldPercentage,
            colors: Array<String>,
            position: Int,
            listener: Listener
        ) {

            textName.setTextColor(Color.BLACK)
            textBuyingPrice.setTextColor(Color.BLACK)
            textSellingPrice.setTextColor(Color.BLACK)

            itemView.setOnClickListener {
                listener.onItemClick(goldType, goldPrice)
            }
            itemView.setBackgroundColor(Color.parseColor(colors[position % 2]))

            textName.text = goldType
            textBuyingPrice.text = goldPrice.buyingPrice.toString()
            textSellingPrice.text = goldPrice.sellingPrice.toString()

            val buyingPercentageText = String.format("%%%.2f", dailyPercentage.buyingPrice)
            textBuyingPercentage.text = buyingPercentageText
            textBuyingPercentage.setTextColor(
                if (dailyPercentage.buyingPrice < 0) Color.RED else Color.GREEN
            )

            val sellingPercentageText = String.format("%%%.2f", dailyPercentage.sellingPrice)
            textSellingPercentage.text = sellingPercentageText
            textSellingPercentage.setTextColor(
                if (dailyPercentage.sellingPrice < 0) Color.RED else Color.GREEN
            )

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_layout_market, parent, false)
        return RowHolder(view)
    }

    override fun getItemCount(): Int {
        return goldPricesMap.size
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        holder.bind(
            goldPricesList[position].first,
            goldPricesList[position].second,
            dailyPercentagesList[position].second,
            colors,
            position,
            listener
        )
    }
}
