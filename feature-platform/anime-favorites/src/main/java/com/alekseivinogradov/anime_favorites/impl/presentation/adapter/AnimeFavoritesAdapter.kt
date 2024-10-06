package com.alekseivinogradov.anime_favorites.impl.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ListItemUi
import com.alekseivinogradov.anime_favorites_platform.databinding.ItemAnimeFavoritesBinding
import com.alekseivinogradov.date.formatter.DateFormatter

internal class AnimeFavoritesAdapter(
    private val itemClickAdapterCallback: (AnimeId) -> Unit,
    private val infoTypeClickAdapterCallback: (AnimeId) -> Unit,
    private val notificationClickAdapterCallback: (AnimeId) -> Unit,
    private val episodesViewedMinusClickAdapterCallback: (AnimeId) -> Unit,
    private val episodesViewedPlusClickAdapterCallback: (AnimeId) -> Unit,
    private val dateFormatter: DateFormatter
) : ListAdapter<ListItemUi, AnimeFavoritesViewHolder>(AnimeFavoritesDiffUtilCallback()) {

    private val itemClickViewHolderCallback: (Int) -> Unit = { adapterPosition: Int ->
        getItem(adapterPosition)?.let {
            itemClickAdapterCallback(it.id)
        }
    }

    private val infoTypeClickViewHolderCallback: (Int) -> Unit = { adapterPosition: Int ->
        getItem(adapterPosition)?.let {
            infoTypeClickAdapterCallback(it.id)
        }
    }

    private val notificationClickViewHolderCallback: (Int) -> Unit = { adapterPosition: Int ->
        getItem(adapterPosition)?.let {
            notificationClickAdapterCallback(it.id)
        }
    }

    private val episodesViewedMinusClickViewHolderCallback: (Int) -> Unit =
        { adapterPosition: Int ->
            getItem(adapterPosition)?.let {
                episodesViewedMinusClickAdapterCallback(it.id)
            }
        }

    private val episodesViewedPlusClickViewHolderCallback: (Int) -> Unit =
        { adapterPosition: Int ->
            getItem(adapterPosition)?.let {
                episodesViewedPlusClickAdapterCallback(it.id)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeFavoritesViewHolder {
        return AnimeFavoritesViewHolder(
            binding = ItemAnimeFavoritesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            itemClickViewHolderCallback = itemClickViewHolderCallback,
            infoTypeClickViewHolderCallback = infoTypeClickViewHolderCallback,
            notificationClickViewHolderCallback = notificationClickViewHolderCallback,
            episodesViewedMinusClickViewHolderCallback = episodesViewedMinusClickAdapterCallback,
            episodesViewedPlusClickViewHolderCallback = episodesViewedPlusClickViewHolderCallback,
            dateFormatter = dateFormatter
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
        if (payloads.isNotEmpty()) {
            payloads.forEach { payloadsList ->
                if (payloadsList !is List<*>) return
                payloadsList.forEach { payload ->
                    if (payload is AnimeFavoritesPayload) {
                        holder.bindWithPayload(payload)
                    }
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }
}
