package com.alekseivinogradov.anime_list.impl.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.presentation.mapper.model.toDomain
import com.alekseivinogradov.anime_list.api.presentation.model.ListItemUi
import com.alekseivinogradov.anime_list_platform.databinding.ItemAnimeListBinding
import com.alekseivinogradov.celebrity.api.domain.formatter.DateFormatter

internal class AnimeListAdapter(
    private val episodesInfoClickAdapterCallback: (ListItemDomain) -> Unit,
    private val notificationClickAdapterCallback: (ListItemDomain) -> Unit,
    private val dateFormatter: DateFormatter
) : PagingDataAdapter<ListItemUi, AnimeListViewHolder>(AnimeListDiffUtilCallback()) {

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
        getItem(position)?.let {
            holder.bind(it)
        }
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
        getItem(adapterPosition)?.let {
            episodesInfoClickAdapterCallback(it.toDomain())
        }
    }

    private fun notificationClickViewHolderCallback(adapterPosition: Int) {
        getItem(adapterPosition)?.let {
            notificationClickAdapterCallback(it.toDomain())
        }
    }
}
