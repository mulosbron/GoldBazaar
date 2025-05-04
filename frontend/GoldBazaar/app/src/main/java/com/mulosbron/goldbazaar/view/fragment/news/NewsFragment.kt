package com.mulosbron.goldbazaar.view.fragment.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.databinding.FragmentNewsBinding
import com.mulosbron.goldbazaar.model.entity.NewsArticle
import com.mulosbron.goldbazaar.view.adapter.NewsAdapter
import com.mulosbron.goldbazaar.viewmodel.news.NewsUiState
import com.mulosbron.goldbazaar.viewmodel.news.NewsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsFragment : Fragment(), NewsAdapter.OnNewsItemClickListener {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private val newsViewModel: NewsViewModel by viewModel()
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        observeViewModel()
        loadNews()
    }

    private fun setupUI() {
        binding.lastUpdatedCombined.text = getString(R.string.loading)
    }

    private fun setupRecyclerView() {
        binding.rvNews.layoutManager = LinearLayoutManager(context)
        newsAdapter = NewsAdapter(this)
        binding.rvNews.adapter = newsAdapter
    }

    private fun observeViewModel() {
        newsViewModel.newsUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NewsUiState.Loading -> {
                    showLoading(true)
                }

                is NewsUiState.Success -> {
                    showLoading(false)
                    binding.emptyStateContainer.visibility = View.GONE
                    binding.rvNews.visibility = View.VISIBLE
                }

                is NewsUiState.Error -> {
                    showLoading(false)
                    if (newsAdapter.itemCount == 0) {
                        binding.emptyStateContainer.visibility = View.VISIBLE
                        binding.rvNews.visibility = View.GONE
                    }
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(resources.getColor(R.color.error, null))
                        .setTextColor(resources.getColor(R.color.white, null))
                        .show()
                }

                is NewsUiState.Empty -> {
                    showLoading(false)
                    binding.emptyStateContainer.visibility = View.VISIBLE
                    binding.rvNews.visibility = View.GONE
                }

                else -> {
                    // İlk durum için bir şey yapmaya gerek yok
                }
            }
        }

        newsViewModel.articles.observe(viewLifecycleOwner) { articles ->
            newsAdapter.submitList(articles)

            if (articles.isEmpty() && newsViewModel.newsUiState.value is NewsUiState.Success) {
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.rvNews.visibility = View.GONE
                binding.emptyStateTitle.text = getString(R.string.empty_news)
                binding.emptyStateDescription.text = getString(R.string.no_news)
            } else if (articles.isNotEmpty()) {
                binding.emptyStateContainer.visibility = View.GONE
                binding.rvNews.visibility = View.VISIBLE
            }
        }

        newsViewModel.lastUpdated.observe(viewLifecycleOwner) { lastUpdated ->
            binding.lastUpdatedCombined.text = getString(R.string.last_update, lastUpdated)
        }
    }

    private fun loadNews() {
        newsViewModel.loadNews()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onNewsItemClick(article: NewsArticle) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}