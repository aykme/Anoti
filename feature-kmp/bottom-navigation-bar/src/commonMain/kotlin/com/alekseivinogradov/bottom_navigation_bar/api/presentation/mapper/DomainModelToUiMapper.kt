package com.alekseivinogradov.bottom_navigation_bar.api.presentation.mapper

import com.alekseivinogradov.bottom_navigation_bar.api.domain.model.SectionDomain
import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.bottom_navigation_bar.api.presentation.model.SectionUi
import com.alekseivinogradov.bottom_navigation_bar.api.presentation.model.UiModel

internal fun mapStateToUiModel(state: BottomNavigationBarStore.State): UiModel {
    return UiModel(
        selectedSection = mapSelectedSectionDomainToUi(state.selectedSection),
        favoritesBadgeNumber = state.favoritesBadgeNumber
    )
}

private fun mapSelectedSectionDomainToUi(selectedSection: SectionDomain): SectionUi {
    return when (selectedSection) {
        SectionDomain.MAIN -> SectionUi.MAIN
        SectionDomain.FAVORITES -> SectionUi.FAVORITES
    }
}
