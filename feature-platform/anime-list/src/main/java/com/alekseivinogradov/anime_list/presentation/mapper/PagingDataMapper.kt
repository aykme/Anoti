package com.alekseivinogradov.anime_list.presentation.mapper

import app.cash.paging.PagingData
import app.cash.paging.map
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi

class PagingDataMapper {

    fun PagingData<ListItemDomain>.toUi(transform: (ListItemDomain) -> ListItemUi) {
        this.map {
            transform(it)
        }
    }
}