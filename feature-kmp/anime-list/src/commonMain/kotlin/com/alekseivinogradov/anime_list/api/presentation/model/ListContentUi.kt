package com.alekseivinogradov.anime_list.api.presentation.model

import app.cash.paging.PagingData
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi

data class ListContentUi(
    val listItems: PagingData<ListItemUi> = PagingData.empty(),
    val isNeedToResetListPositon: Boolean = false
)