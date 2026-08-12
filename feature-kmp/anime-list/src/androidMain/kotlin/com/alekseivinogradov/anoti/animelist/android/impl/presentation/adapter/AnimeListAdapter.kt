package com.alekseivinogradov.anoti.animelist.android.impl.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.alekseivinogradov.anoti.animelist.kmp.R
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter

internal class AnimeListAdapter(
    private val episodesInfoClickAdapterCallback: (AnimeId) -> Unit,
    private val notificationClickAdapterCallback: (AnimeId) -> Unit,
    private val dateFormatter: DateFormatter,
    private val coroutineContextProvider: CoroutineContextProvider
) : ListAdapter<ListItemUi, AnimeListViewHolder>(AnimeListDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeListViewHolder {
        return AnimeListViewHolder(
            itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_anime_list,
                parent,
                false
            ),
            episodesInfoClickViewHolderCallback = ::episodesInfoClickViewHolderCallback,
            notificationClickViewHolderCallback = ::notificationClickViewHolderCallback,
            dateFormatter = dateFormatter,
            coroutineContextProvider = coroutineContextProvider
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
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        for (payloadsList in payloads) {
            if (payloadsList !is List<*>) return
            for (payload in payloadsList) {
                if (payload is AnimeListPayload) {
                    holder.bindWithPayload(payload)
                }
            }
        }
    }

    private fun episodesInfoClickViewHolderCallback(adapterPosition: Int) {
        episodesInfoClickAdapterCallback(getItem(adapterPosition).id)
    }

    private fun notificationClickViewHolderCallback(adapterPosition: Int) {
        notificationClickAdapterCallback(getItem(adapterPosition).id)
    }
}
