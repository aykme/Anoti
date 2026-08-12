package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.alekseivinogradov.anoti.animefavorites.kmp.R
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter

// One parameter per click callback plus its two dependencies, not incidental parameter creep.
@Suppress("LongParameterList")
internal class AnimeFavoritesAdapter(
    private val itemClickAdapterCallback: (AnimeId) -> Unit,
    private val infoTypeClickAdapterCallback: (AnimeId) -> Unit,
    private val notificationClickAdapterCallback: (AnimeId) -> Unit,
    private val episodesViewedMinusClickAdapterCallback: (AnimeId) -> Unit,
    private val episodesViewedPlusClickAdapterCallback: (AnimeId) -> Unit,
    private val dateFormatter: DateFormatter,
    private val coroutineContextProvider: CoroutineContextProvider
) : ListAdapter<ListItemUi, AnimeFavoritesViewHolder>(AnimeFavoritesDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeFavoritesViewHolder {
        return AnimeFavoritesViewHolder(
            itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_anime_favorites,
                parent,
                false
            ),
            itemClickViewHolderCallback = ::itemClickViewHolderCallback,
            infoTypeClickViewHolderCallback = ::infoTypeClickViewHolderCallback,
            notificationClickViewHolderCallback = ::notificationClickViewHolderCallback,
            episodesViewedMinusClickViewHolderCallback = ::episodesViewedMinusClickViewHolderCallback,
            episodesViewedPlusClickViewHolderCallback = ::episodesViewedPlusClickViewHolderCallback,
            dateFormatter = dateFormatter,
            coroutineContextProvider = coroutineContextProvider
        )
    }

    override fun onBindViewHolder(holder: AnimeFavoritesViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    override fun onBindViewHolder(
        holder: AnimeFavoritesViewHolder,
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
                if (payload is AnimeFavoritesPayload) {
                    holder.bindWithPayload(payload)
                }
            }
        }
    }

    private fun itemClickViewHolderCallback(adapterPosition: Int) {
        getItem(adapterPosition)?.let {
            itemClickAdapterCallback(it.id)
        }
    }

    private fun infoTypeClickViewHolderCallback(adapterPosition: Int) {
        getItem(adapterPosition)?.let {
            infoTypeClickAdapterCallback(it.id)
        }
    }

    private fun notificationClickViewHolderCallback(adapterPosition: Int) {
        getItem(adapterPosition)?.let {
            notificationClickAdapterCallback(it.id)
        }
    }

    private fun episodesViewedMinusClickViewHolderCallback(adapterPosition: Int) {
        getItem(adapterPosition)?.let {
            episodesViewedMinusClickAdapterCallback(it.id)
        }
    }

    private fun episodesViewedPlusClickViewHolderCallback(adapterPosition: Int) {
        getItem(adapterPosition)?.let {
            episodesViewedPlusClickAdapterCallback(it.id)
        }
    }
}
