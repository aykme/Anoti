package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionExecutor
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.SEARCH_DEBOUNCE_MILLISECONDS
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.PageLoadResult
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.Paginator
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchSectionExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: SearchUsecases,
    private val toastProvider: ToastProvider
) : SearchSectionExecutor() {

    private var searchFlow: MutableStateFlow<String>? = null
    private var changeSearchJob: Job? = null
    private var updateSectionJob: Job? = null
    private var loadNextPageJob: Job? = null
    private val updateAnimeDetailsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private var paginator: Paginator<ListItemDomain> = createPaginator(searchText = "")

    override fun executeIntent(intent: SearchSectionStore.Intent) {
        when (intent) {
            SearchSectionStore.Intent.OpenSection -> openSection()
            SearchSectionStore.Intent.UpdateSection -> updateSection()
            SearchSectionStore.Intent.LoadNextPage -> loadNextPage()

            is SearchSectionStore.Intent.ChangeSearchText -> changeSearchText(intent)

            is SearchSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
        }
    }

    private fun createPaginator(searchText: String = state().searchText): Paginator<ListItemDomain> {
        return Paginator(
            firstPage = FIRST_PAGE,
            loadPage = { page ->
                usecases.fetchAnimeListBySearchUsecase.execute(
                    page = page,
                    searchText = searchText
                )
            }
        )
    }

    private fun openSection() {
        publish(SearchSectionStore.Label.ResetListPositionAfterUpdate)
        subscribeSearchFlowIfNeeded()
    }

    @OptIn(FlowPreview::class)
    private fun subscribeSearchFlowIfNeeded() {
        if (searchFlow == null) {
            searchFlow = MutableStateFlow(state().searchText)
        }
        if (changeSearchJob?.isActive == true) return
        changeSearchJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            searchFlow?.debounce(SEARCH_DEBOUNCE_MILLISECONDS)
                ?.collect {
                    updateSection()
                }
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        loadNextPageJob?.cancel()
        paginator = createPaginator()
        updateSectionJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            dispatch(
                SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            dispatch(
                SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoIds = setOf()
                )
            )
            dispatch(
                SearchSectionStore.Message.UpdateAnimeDetails(
                    animeDetails = AnimeDetails()
                )
            )
            publish(SearchSectionStore.Label.ResetListPositionAfterUpdate)
            when (val result = paginator.loadFirstPage()) {
                is PageLoadResult.Success -> {
                    dispatch(SearchSectionStore.Message.UpdateListItems(result.items))
                    dispatch(
                        SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                    )
                }

                is PageLoadResult.Error -> {
                    toastProvider.makeConnectionErrorToast()
                    dispatch(
                        SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }

                is PageLoadResult.UnexpectedError -> {
                    toastProvider.makeUnknownErrorToast()
                    dispatch(
                        SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        loadNextPageJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            when (val result = paginator.loadNextPage()) {
                is PageLoadResult.Success -> dispatch(
                    SearchSectionStore.Message.UpdateListItems(
                        state().sectionContent.listItems + result.items
                    )
                )

                is PageLoadResult.Error -> toastProvider.makeConnectionErrorToast()
                is PageLoadResult.UnexpectedError -> toastProvider.makeUnknownErrorToast()
                null -> Unit
            }
        }
    }

    private fun changeSearchText(intent: SearchSectionStore.Intent.ChangeSearchText) {
        dispatch(SearchSectionStore.Message.ChangeSearchText(intent.searchText))
        searchFlow?.update { state().searchText }
    }

    private fun episodeInfoClick(intent: SearchSectionStore.Intent.EpisodesInfoClick) {
        val listItem = state().sectionContent.listItems.find { it.id == intent.id } ?: return
        if (state().sectionContent.enabledExtraEpisodesInfoIds.contains(listItem.id)) {
            availableEpisodesInfoClick(listItem)
        } else {
            extraEpisodesInfoClick(listItem)
        }
    }

    private fun availableEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds = state()
            .sectionContent
            .enabledExtraEpisodesInfoIds
            .toMutableSet().apply {
                remove(listItem.id)
            }.toSet()

        dispatch(
            SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }

    private fun extraEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds = state()
            .sectionContent
            .enabledExtraEpisodesInfoIds
            .toMutableSet().apply {
                add(listItem.id)
            }.toSet()

        dispatch(
            SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
        if (
            listItem.releaseStatus == ReleaseStatusDomain.ONGOING &&
            !state().sectionContent.animeDetails.nextEpisodesInfo.contains(listItem.id)
        ) {
            updateAnimeDetails(listItem.id)
        }
    }

    private fun updateAnimeDetails(id: Int) {
        updateAnimeDetailsJobMap[id]?.cancel()
        updateAnimeDetailsJobMap[id] = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            val result = usecases
                .fetchAnimeDetailsByIdUsecase
                .execute(id)

            when (result) {
                is CallResult.Success -> onSuccessUpdateAnimeDetails(
                    updateListItem = result.value
                )

                is CallResult.HttpError,
                is CallResult.OtherError -> toastProvider.makeConnectionErrorToast()
            }
        }
    }

    private fun onSuccessUpdateAnimeDetails(updateListItem: ListItemDomain) {
        val newNextEpisodesInfo = state()
            .sectionContent
            .animeDetails
            .nextEpisodesInfo
            .toMutableMap().apply {
                this[updateListItem.id] = updateListItem.nextEpisodeAt
            }

        dispatch(
            SearchSectionStore.Message.UpdateAnimeDetails(
                animeDetails = state().sectionContent.animeDetails.copy(
                    nextEpisodesInfo = newNextEpisodesInfo
                )
            )
        )
    }
}
