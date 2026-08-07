package com.alekseivinogradov.anime_list.api.presentation.model

data class ListContentUi(
    val listItems: List<ListItemUi> = emptyList(),
    val isNeedToResetListPositon: Boolean = false
)