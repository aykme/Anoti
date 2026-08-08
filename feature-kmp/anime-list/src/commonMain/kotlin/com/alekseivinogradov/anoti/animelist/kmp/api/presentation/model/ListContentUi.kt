package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model

data class ListContentUi(
    val listItems: List<ListItemUi> = emptyList(),
    val isNeedToResetListPositon: Boolean = false
)
