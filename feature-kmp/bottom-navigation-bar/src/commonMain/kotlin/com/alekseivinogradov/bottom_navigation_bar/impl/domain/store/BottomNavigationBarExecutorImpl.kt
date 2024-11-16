package com.alekseivinogradov.bottom_navigation_bar.impl.domain.store

import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarExecutor
import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore

internal class BottomNavigationBarExecutorImpl() : BottomNavigationBarExecutor() {
    override fun executeIntent(intent: BottomNavigationBarStore.Intent) {
        when (intent) {
            is BottomNavigationBarStore.Intent.ChangeSelectedSection -> {
                changeSelectedSection(intent)
            }

            is BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber -> {
                updateFavoritesBadgeNumber(intent)
            }

            BottomNavigationBarStore.Intent.MainSectionClick -> mainSectionClick()
            BottomNavigationBarStore.Intent.FavoritesSectionClick -> favoritesSectionClick()
        }
    }

    private fun changeSelectedSection(
        intent: BottomNavigationBarStore.Intent.ChangeSelectedSection
    ) {
        dispatch(
            BottomNavigationBarStore.Message.ChangeSelectedSection(intent.selectedSection)
        )
    }

    private fun updateFavoritesBadgeNumber(
        intent: BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber
    ) {
        dispatch(
            BottomNavigationBarStore.Message.UpdateFavoritesBadgeNumber(intent.favoritesBadgeNumber)
        )
    }

    private fun mainSectionClick() {
        publish(
            BottomNavigationBarStore.Label.NavigateToMain
        )
    }

    private fun favoritesSectionClick() {
        publish(
            BottomNavigationBarStore.Label.NavigateToFavorites
        )
    }
}
