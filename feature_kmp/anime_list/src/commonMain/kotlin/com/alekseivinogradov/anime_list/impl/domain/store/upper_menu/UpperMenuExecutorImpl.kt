package com.alekseivinogradov.anime_list.impl.domain.store.upper_menu

import com.alekseivinogradov.anime_list.api.domain.model.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionDomain
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias UpperMenuExecutor = CoroutineExecutor<
        UpperMenuStore.Intent,
        Unit, UpperMenuStore.State,
        UpperMenuReducer.Message,
        UpperMenuStore.Label
        >

internal class UpperMenuExecutorImpl() : UpperMenuExecutor() {

    override fun executeIntent(
        intent: UpperMenuStore.Intent,
        getState: () -> UpperMenuStore.State
    ) {
        when (intent) {
            UpperMenuStore.Intent.OngoingsSectionClick -> ongoingSectionClick(getState)
            UpperMenuStore.Intent.AnnouncedSectionClick -> announcedSectionClick(getState)
            UpperMenuStore.Intent.SearchSectionClick -> searchSectionClick(getState)
            UpperMenuStore.Intent.CancelSearchClick -> cancelSearchClick(getState)
            is UpperMenuStore.Intent.SearchTextChange -> searchTextChange(
                getState = getState,
                intent = intent
            )
        }
    }

    private fun ongoingSectionClick(getState: () -> UpperMenuStore.State) {
        when (getState().selectedSection) {
            SectionDomain.ONGOINGS -> Unit
            SectionDomain.ANNOUNCED,
            SectionDomain.SEARCH,
            -> dispatch(
                UpperMenuReducer.Message.ChangeSelectedSection(
                    selectedSection = SectionDomain.ONGOINGS
                )
            )
        }
    }

    private fun announcedSectionClick(getState: () -> UpperMenuStore.State) {
        when (getState().selectedSection) {
            SectionDomain.ANNOUNCED -> Unit
            SectionDomain.ONGOINGS,
            SectionDomain.SEARCH,
            -> dispatch(
                UpperMenuReducer.Message.ChangeSelectedSection(
                    selectedSection = SectionDomain.ANNOUNCED
                )
            )
        }
    }

    private fun searchSectionClick(getState: () -> UpperMenuStore.State) {
        val state = getState()
        when (state.selectedSection) {
            SectionDomain.SEARCH -> Unit
            SectionDomain.ONGOINGS,
            SectionDomain.ANNOUNCED,
            -> dispatch(
                UpperMenuReducer.Message.ChangeSelectedSection(
                    selectedSection = SectionDomain.SEARCH
                )
            )
        }
        dispatch(
            UpperMenuReducer.Message.ChangeSearch(
                search = state.search.copy(
                    type = SearchDomain.Type.SHOWN
                )
            )
        )
    }

    private fun cancelSearchClick(getState: () -> UpperMenuStore.State) {
        dispatch(
            UpperMenuReducer.Message.ChangeSearch(
                search = getState().search.copy(
                    type = SearchDomain.Type.HIDEN
                )
            )
        )
    }

    private fun searchTextChange(
        getState: () -> UpperMenuStore.State,
        intent: UpperMenuStore.Intent.SearchTextChange
    ) {
        dispatch(
            UpperMenuReducer.Message.ChangeSearch(
                search = getState().search.copy(
                    searchText = intent.text
                )
            )
        )
    }
}