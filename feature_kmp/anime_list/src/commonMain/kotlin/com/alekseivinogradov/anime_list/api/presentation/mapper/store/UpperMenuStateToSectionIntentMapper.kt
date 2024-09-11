package com.alekseivinogradov.anime_list.api.presentation.mapper.store

import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SectionDomain
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuStore

internal fun mapUpperMenuStateToOngoingSectionIntent(state: UpperMenuStore.State):
        OngoingSectionStore.Intent? {
    return when (state.selectedSection) {
        SectionDomain.ONGOINGS -> OngoingSectionStore.Intent.OpenSection
        SectionDomain.ANNOUNCED,
        SectionDomain.SEARCH -> null
    }
}

internal fun mapUpperMenuStateToAnnouncedSectionIntent(state: UpperMenuStore.State):
        AnnouncedSectionStore.Intent? {
    return when (state.selectedSection) {
        SectionDomain.ANNOUNCED -> AnnouncedSectionStore.Intent.OpenSection
        SectionDomain.ONGOINGS,
        SectionDomain.SEARCH -> null
    }
}

internal fun mapUpperMenuStateToSearchSectionIntent(state: UpperMenuStore.State):
        SearchSectionStore.Intent? {
    return when (state.selectedSection) {
        SectionDomain.SEARCH -> SearchSectionStore.Intent.OpenSection
        SectionDomain.ANNOUNCED,
        SectionDomain.ONGOINGS -> null
    }
}