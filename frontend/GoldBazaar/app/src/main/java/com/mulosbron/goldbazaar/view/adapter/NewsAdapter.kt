package com.mulosbron.goldbazaar.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.model.entity.NewsArticle
import com.mulosbron.goldbazaar.util.SimpleImageLoader

/**
 * NewsAdapter - RecyclerView için haber adaptörü
 */
class NewsAdapter(
    private val listener: OnNewsItemClickListener
) : ListAdapter<NewsArticle, NewsAdapter.NewsViewHolder>(ArticleDiffCallback()) {

    /**
     * Haber öğesi tıklama olayı dinleyici arayüzü
     */
    interface OnNewsItemClickListener {
        fun onNewsItemClick(article: NewsArticle)
    }

    /**
     * NewsViewHolder - Haber öğesi görünümünü tutan ViewHolder
     */
    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageCard: MaterialCardView = itemView.findViewById(R.id.imageCard)
        private val newsImage: ImageView = itemView.findViewById(R.id.newsImage)
        private val newsTitle: TextView = itemView.findViewById(R.id.newsTitle)
        private val newsDescription: TextView = itemView.findViewById(R.id.newsDescription)
        private val newsSource: TextView = itemView.findViewById(R.id.newsSource)
        private val readMoreButton: MaterialButton = itemView.findViewById(R.id.readMoreButton)

        /**
         * Görünümü veriler ile bağlar
         */
        fun bind(article: NewsArticle) {
            // Başlık, açıklama ve kaynak bilgilerini ayarla
            newsTitle.text = article.title
            newsDescription.text = article.getShortDescription()
            newsSource.text = article.getFormattedSource()

            // Görseli yükle
            if (!article.urlToImage.isNullOrEmpty()) {
                SimpleImageLoader.loadImage(
                    newsImage,
                    article.urlToImage,
                    R.drawable.ic_image,
                    R.drawable.ic_broken_image
                )
                imageCard.visibility = View.VISIBLE
            } else {
                imageCard.visibility = View.GONE
            }

            // Öğeye tıklama olayını ayarla
            itemView.setOnClickListener {
                listener.onNewsItemClick(article)
            }

            // "Daha Fazla Oku" butonuna tıklama olayını ayarla
            readMoreButton.setOnClickListener {
                listener.onNewsItemClick(article)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = getItem(position)
        holder.bind(article)
    }
}

/**
 * ArticleDiffCallback - Article nesneleri için DiffUtil callback
 */
private class ArticleDiffCallback : DiffUtil.ItemCallback<NewsArticle>() {
    override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
        return oldItem == newItem
    }
}