package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model

/**
 * The selected section's list content, ready for display.
 *
 * @param listItems the list items.
 * @param isNeedToResetListPositon whether the list's scroll position should reset (e.g. after
 * the section changed).
 */
data class ListContentUi(
    val listItems: List<ListItemUi> = emptyList(),
    val isNeedToResetListPositon: Boolean = false
)
