package com.alekseivinogradov.anime_list.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.alekseivinogradov.anime_list.api.model.list_content.UiListItem
import com.alekseivinogradov.anime_list.databinding.ItemAnimeListBinding

internal class AnimeListAdapter(
    private val episodesInfoClickCallback: (Int) -> Unit,
    private val notificationClickCallback: (Int) -> Unit
) : ListAdapter<UiListItem, AnimeListViewHolder>(AnimeListDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeListViewHolder {
        return AnimeListViewHolder(
            binding = ItemAnimeListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            episodesInfoClickCallback = episodesInfoClickCallback,
            notificationClickCallback = notificationClickCallback
        )
    }

    override fun onBindViewHolder(holder: AnimeListViewHolder, position: Int) {
        holder.bind(item = getItem(position))
    }

//    override fun onBindViewHolder(
//        holder: AnimeListViewHolder,
//        position: Int,
//        payloads: MutableList<Any>
//    ) {
//        payloads.forEach { payloadsList ->
//            if (payloadsList !is List<*>) return
//
//            payloadsList.forEach { payload ->
//                if (payload is AnimeListPayload) {
//                    holder.bindWithPayload(payload = payload)
//                }
//            }
//        }
//    }
}