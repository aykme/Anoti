package com.alekseivinogradov.anoti.animelist.platform.impl.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.platform.databinding.ItemAnimeListBinding
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter

internal class AnimeListAdapter(
    private val episodesInfoClickAdapterCallback: (AnimeId) -> Unit,
    private val notificationClickAdapterCallback: (AnimeId) -> Unit,
    private val dateFormatter: DateFormatter
) : ListAdapter<ListItemUi, AnimeListViewHolder>(AnimeListDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeListViewHolder {
        return AnimeListViewHolder(
            binding = ItemAnimeListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            episodesInfoClickViewHolderCallback = ::episodesInfoClickViewHolderCallback,
            notificationClickViewHolderCallback = ::notificationClickViewHolderCallback,
            dateFormatter = dateFormatter
        )
    }

    override fun onBindViewHolder(holder: AnimeListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: AnimeListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            payloads.forEach { payloadsList ->
                if (payloadsList !is List<*>) return
                payloadsList.forEach { payload ->
                    if (payload is AnimeListPayload) {
                        holder.bindWithPayload(payload)
                    }
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    private fun episodesInfoClickViewHolderCallback(adapterPosition: Int) {
        episodesInfoClickAdapterCallback(getItem(adapterPosition).id)
    }

    private fun notificationClickViewHolderCallback(adapterPosition: Int) {
        notificationClickAdapterCallback(getItem(adapterPosition).id)
    }
}
