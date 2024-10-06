package com.alekseivinogradov.anime_favorites.api.presentation.model

import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ListItemUi

data class UiModel(
    val listItems: List<ListItemUi> = listOf()
)
