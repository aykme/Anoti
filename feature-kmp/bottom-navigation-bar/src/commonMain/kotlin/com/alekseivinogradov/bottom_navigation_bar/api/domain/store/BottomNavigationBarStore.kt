package com.alekseivinogradov.bottom_navigation_bar.api.domain.store

import com.alekseivinogradov.bottom_navigation_bar.api.domain.model.SectionDomain
import com.arkivanov.mvikotlin.core.store.Store

interface BottomNavigationBarStore : Store<
        BottomNavigationBarStore.Intent,
        BottomNavigationBarStore.State,
        BottomNavigationBarStore.Label
        > {

    data class State(
        val selectedSection: SectionDomain = SectionDomain.MAIN,
        val favoritesBadgeNumber: Int = 0
    )

    sealed interface Intent {
        data class ChangeSelectedSection(val selectedSection: SectionDomain) : Intent
        data class UpdateFavoritesBadgeNumber(val favoritesBadgeNumber: Int) : Intent
        data object MainSectionClick : Intent
        data object FavoritesSectionClick : Intent
    }

    sealed interface Label {
        data object NavigateToMain : Label
        data object NavigateToFavorites : Label
    }

    sealed interface Action

    sealed interface Message {
        data class ChangeSelectedSection(val selectedSection: SectionDomain) : Message
        data class UpdateFavoritesBadgeNumber(val favoritesBadgeNumber: Int) : Message
    }
}
