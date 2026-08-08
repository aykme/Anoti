package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model

import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi

data class UiModel(
    val listItems: List<ListItemUi> = listOf(),
    val contentType: ContentTypeUi = ContentTypeUi.LOADING,
)
