package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model

import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * What the favorites screen should render.
 *
 * @param listItems favorites list items, ready for display.
 * @param contentType loading state of the list.
 */
data class UiModel(
    val listItems: ImmutableList<ListItemUi> = persistentListOf(),
    val contentType: ContentTypeUi = ContentTypeUi.LOADING,
)
