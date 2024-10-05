package com.alekseivinogradov.bottom_navigation_bar.api.presentation.model

data class UiModel(
    val selectedSection: SectionUi = SectionUi.MAIN,
    val favoritesBadgeNumber: Int = 0
)
