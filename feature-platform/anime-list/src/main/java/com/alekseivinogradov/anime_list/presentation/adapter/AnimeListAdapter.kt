package com.alekseivinogradov.anime_list.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.alekseivinogradov.animeListPlatform.databinding.ItemAnimeListBinding
import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi
import com.alekseivinogradov.date.formatter.DateFormatter

internal class AnimeListAdapter(
    private val episodesInfoClickAdapterCallback: (AnimeId) -> Unit,
    private val notificationClickAdapterCallback: (AnimeId) -> Unit,
    private val dateFormatter: DateFormatter
) : ListAdapter<ListItemUi, AnimeListViewHolder>(AnimeListDiffUtilCallback()) {

    private val episodesInfoClickViewHolderCallback: (Int) -> Unit = { adapterPosition: Int ->
        episodesInfoClickAdapterCallback(getItem(adapterPosition).id)
    }

    private val notificationClickViewHolderCallback: (Int) -> Unit = { adapterPosition: Int ->
        notificationClickAdapterCallback(getItem(adapterPosition).id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeListViewHolder {
        return AnimeListViewHolder(
            binding = ItemAnimeListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            episodesInfoClickViewHolderCallback = episodesInfoClickViewHolderCallback,
            notificationClickViewHolderCallback = notificationClickViewHolderCallback,
            dateFormatter = dateFormatter
        )
    }

    override fun onBindViewHolder(holder: AnimeListViewHolder, position: Int) {
        holder.bind(item = getItem(position))
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
                        holder.bindWithPayload(payload = payload)
                    }
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }
}
