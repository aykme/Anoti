package com.alekseivinogradov.anime_list.impl.presentation.mapper.model

import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionUi
import com.alekseivinogradov.anime_list.impl.domain.model.SearchDomain
import com.alekseivinogradov.anime_list.impl.domain.model.SectionDomain
import com.alekseivinogradov.anime_list.impl.domain.store.upper_menu.UpperMenuStore

internal fun mapStateToUiModel(
    upperMenuState: UpperMenuStore.State
): AnimeListView.UiModel {
    return AnimeListView.UiModel(
        selectedSection = getSelectedSection(upperMenuState.selectedSection),
        search = getSearch(upperMenuState.search)
    )
}

private fun getSelectedSection(selectedSection: SectionDomain): SectionUi {
    return when (selectedSection) {
        SectionDomain.ONGOINGS -> SectionUi.ONGOINGS
        SectionDomain.ANNOUNCED -> SectionUi.ANNOUNCED
        SectionDomain.SEARCH -> SectionUi.SEARCH
    }
}

private fun getSearch(search: SearchDomain): SearchUi {
    return when (search.type) {
        SearchDomain.Type.HIDEN -> SearchUi.HIDEN
        SearchDomain.Type.SHOWN -> SearchUi.SHOWN
    }
}