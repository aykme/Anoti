package com.alekseivinogradov.anime_list.impl.domain.store.upper_menu

import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SectionDomain
import com.arkivanov.mvikotlin.core.store.Store

internal interface UpperMenuStore :
    Store<UpperMenuStore.Intent, UpperMenuStore.State, UpperMenuStore.Label> {

    data class State(
        val selectedSection: SectionDomain = SectionDomain.ONGOINGS,
        val search: SearchDomain = SearchDomain()
    )

    sealed interface Intent {
        data object OngoingsSectionClick : Intent
        data object AnnouncedSectionClick : Intent
        data object SearchSectionClick : Intent
        data object CancelSearchClick : Intent
        data class SearchTextChange(val text: String) : Intent
    }

    sealed interface Label {
        data object UpdateOngoingsSection : Label
        data object UpdateAnnouncedSection : Label
        data object UpdateSearchSection : Label
    }
}
