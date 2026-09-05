package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model

/**
 * What the bottom navigation bar should render.
 *
 * @param selectedSection currently selected section.
 * @param favoritesBadgeNumber number shown on the favorites tab's badge.
 */
data class BottomNavigationBarUiModel(
    val selectedSection: SectionUi = SectionUi.MAIN,
    val favoritesBadgeNumber: Int = 0
)
