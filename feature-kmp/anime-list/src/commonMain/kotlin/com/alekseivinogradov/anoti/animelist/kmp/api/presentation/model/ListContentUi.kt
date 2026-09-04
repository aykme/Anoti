package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The selected section's list content, ready for display.
 *
 * @param listItems the list items.
 * @param isNeedToResetListPositon whether the list's scroll position should reset (e.g. after
 * the section changed).
 */
data class ListContentUi(
    val listItems: ImmutableList<ListItemUi> = persistentListOf(),
    val isNeedToResetListPositon: Boolean = false
)
