package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.BottomNavigationBarUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.SectionUi

fun mapStateToUiModel(state: BottomNavigationBarStore.State): BottomNavigationBarUiModel {
    return BottomNavigationBarUiModel(
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
