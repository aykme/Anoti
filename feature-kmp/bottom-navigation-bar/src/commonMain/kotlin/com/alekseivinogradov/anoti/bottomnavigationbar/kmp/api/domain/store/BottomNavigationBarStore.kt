package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.arkivanov.mvikotlin.core.store.Store

/**
 * The store for the bottom navigation bar.
 */
interface BottomNavigationBarStore : Store<
    BottomNavigationBarStore.Intent,
    BottomNavigationBarStore.State,
    BottomNavigationBarStore.Label
    > {

    /**
     * @param selectedSection currently selected section.
     * @param favoritesBadgeNumber number shown on the favorites tab's badge.
     */
    data class State(
        val selectedSection: SectionDomain = SectionDomain.MAIN,
        val favoritesBadgeNumber: Int = 0
    )

    /** Actions a caller can dispatch via [accept]. */
    sealed interface Intent {
        /** External navigation moved to [selectedSection]; sync the bar's selection. */
        data class ChangeSelectedSection(val selectedSection: SectionDomain) : Intent

        /** Update the favorites tab's badge to [favoritesBadgeNumber]. */
        data class UpdateFavoritesBadgeNumber(val favoritesBadgeNumber: Int) : Intent

        /** The user tapped the main tab. */
        data object MainSectionClick : Intent

        /** The user tapped the favorites tab. */
        data object FavoritesSectionClick : Intent
    }

    /** One-off events the store publishes for callers to react to. */
    sealed interface Label {
        /** Navigate to the main section. */
        data object NavigateToMain : Label

        /** Navigate to the favorites section. */
        data object NavigateToFavorites : Label
    }

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Action

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Message {
        /**
         * Replaces [State.selectedSection].
         *
         * @param selectedSection the newly selected section.
         */
        data class ChangeSelectedSection(val selectedSection: SectionDomain) : Message

        /**
         * Replaces [State.favoritesBadgeNumber].
         *
         * @param favoritesBadgeNumber the badge's new number.
         */
        data class UpdateFavoritesBadgeNumber(val favoritesBadgeNumber: Int) : Message
    }
}
