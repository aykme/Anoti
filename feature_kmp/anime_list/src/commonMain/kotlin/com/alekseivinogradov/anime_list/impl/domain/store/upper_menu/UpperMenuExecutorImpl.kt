package com.alekseivinogradov.anime_list.impl.domain.store.upper_menu

import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SectionDomain
import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuExecutor
import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuStore

internal class UpperMenuExecutorImpl() : UpperMenuExecutor() {

    override fun executeIntent(intent: UpperMenuStore.Intent) {
        when (intent) {
            UpperMenuStore.Intent.OngoingsSectionClick -> ongoingSectionClick()
            UpperMenuStore.Intent.AnnouncedSectionClick -> announcedSectionClick()
            UpperMenuStore.Intent.SearchSectionClick -> searchSectionClick()
            UpperMenuStore.Intent.CancelSearchClick -> cancelSearchClick()
            is UpperMenuStore.Intent.SearchTextChange -> searchTextChange(intent)
        }
    }

    private fun ongoingSectionClick() {
        when (state().selectedSection) {
            SectionDomain.ONGOINGS -> Unit
            SectionDomain.ANNOUNCED,
            SectionDomain.SEARCH,
            -> dispatch(
                UpperMenuStore.Message.ChangeSelectedSection(
                    selectedSection = SectionDomain.ONGOINGS
                )
            )
        }
    }

    private fun announcedSectionClick() {
        when (state().selectedSection) {
            SectionDomain.ANNOUNCED -> Unit
            SectionDomain.ONGOINGS,
            SectionDomain.SEARCH,
            -> dispatch(
                UpperMenuStore.Message.ChangeSelectedSection(
                    selectedSection = SectionDomain.ANNOUNCED
                )
            )
        }
    }

    private fun searchSectionClick() {
        val state = state()
        when (state.selectedSection) {
            SectionDomain.SEARCH -> Unit
            SectionDomain.ONGOINGS,
            SectionDomain.ANNOUNCED,
            -> dispatch(
                UpperMenuStore.Message.ChangeSelectedSection(
                    selectedSection = SectionDomain.SEARCH
                )
            )
        }
        dispatch(
            UpperMenuStore.Message.ChangeSearch(
                search = state.search.copy(
                    type = SearchDomain.Type.SHOWN
                )
            )
        )
    }

    private fun cancelSearchClick() {
        dispatch(
            UpperMenuStore.Message.ChangeSearch(
                search = state().search.copy(
                    type = SearchDomain.Type.HIDEN
                )
            )
        )
    }

    private fun searchTextChange(intent: UpperMenuStore.Intent.SearchTextChange) {
        dispatch(
            UpperMenuStore.Message.ChangeSearch(
                search = state().search.copy(
                    searchText = intent.searchText
                )
            )
        )
    }
}