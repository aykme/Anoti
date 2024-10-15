package com.alekseivinogradov.anime_list.api.presentation.model

import app.cash.paging.PagingData

data class ListContentUi(
    val listItems: PagingData<ListItemUi> = PagingData.empty(),
    val isNeedToResetListPositon: Boolean = false
)