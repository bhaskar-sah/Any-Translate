package com.bhaskar.anytranslate.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bhaskar.anytranslate.R
import com.bhaskar.anytranslate.data.local.TranslationHistoryEntity
import com.bhaskar.anytranslate.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onItemClick: (String) -> Unit,
    private val onToggleFavorite: (TranslationHistoryEntity) -> Unit,
    private val onDeleteClick: (Long) -> Unit
) : ListAdapter<TranslationHistoryEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TranslationHistoryEntity) {
            binding.tvQuery.text = item.queryText
            binding.tvTranslationsSummary.text = "DE: ${item.germanTranslation} • NE: ${item.nepaliTranslation} • HI: ${item.hindiTranslation}"

            val iconRes = if (item.isFavorite) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
            binding.btnToggleBookmark.setIconResource(iconRes)

            binding.root.setOnClickListener {
                onItemClick(item.queryText)
            }

            binding.btnToggleBookmark.setOnClickListener {
                onToggleFavorite(item)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(item.id)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TranslationHistoryEntity>() {
        override fun areItemsTheSame(
            oldItem: TranslationHistoryEntity,
            newItem: TranslationHistoryEntity
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: TranslationHistoryEntity,
            newItem: TranslationHistoryEntity
        ): Boolean = oldItem == newItem
    }
}
