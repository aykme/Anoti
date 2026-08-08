package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.SectionUi
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.UiModel

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
