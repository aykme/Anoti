package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.AnimeDetails
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.domain.paging.SearchListDataSource
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.celebrity.api.domain.PAGING_PREFETCH_DISTANCE
import com.alekseivinogradov.celebrity.api.domain.SEARCH_DEBOUNCE_MILLISECONDS
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
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
    private val updateAnimeDetailsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()

    override fun executeIntent(intent: SearchSectionStore.Intent) {
        when (intent) {
            SearchSectionStore.Intent.OpenSection -> openSection()
            SearchSectionStore.Intent.UpdateSection -> updateSection()

            is SearchSectionStore.Intent.ChangeSearchText -> ChangesearchText(intent)

            is SearchSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
        }
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
            getPagingDataFlow().collect { listItems: PagingData<ListItemDomain> ->
                publish(SearchSectionStore.Label.ResetListPositionAfterUpdate)
                dispatch(SearchSectionStore.Message.UpdateListItems(listItems))
            }
        }
    }

    private fun getPagingDataFlow(): Flow<PagingData<ListItemDomain>> {
        return Pager(
            config = PagingConfig(
                pageSize = ITEMS_PER_PAGE,
                prefetchDistance = PAGING_PREFETCH_DISTANCE,
                enablePlaceholders = true
            )
        ) {
            SearchListDataSource(
                fetchAnimeListBySearchUsecase = usecases.fetchAnimeListBySearchUsecase,
                searchText = state().searchText,
                toastProvider = toastProvider,
                initialLoadSuccessCallback = ::initialLoadSuccessCallback,
                initialLoadErrorCallback = ::initialLoadErrorCallback
            )
        }.flow.catch { toastProvider.makeUnknownErrorToast() }
            .cachedIn(scope)
    }

    private fun initialLoadSuccessCallback() {
        dispatch(
            SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
        )
    }

    private fun initialLoadErrorCallback() {
        dispatch(
            SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
        )
    }

    private fun ChangesearchText(intent: SearchSectionStore.Intent.ChangeSearchText) {
        dispatch(SearchSectionStore.Message.ChangeSearchText(intent.searchText))
        searchFlow?.update { state().searchText }
    }

    private fun episodeInfoClick(intent: SearchSectionStore.Intent.EpisodesInfoClick) {
        if (state().sectionContent.enabledExtraEpisodesInfoIds.contains(intent.listItem.id)) {
            availableEpisodesInfoClick(intent.listItem)
        } else {
            extraEpisodesInfoClick(intent.listItem)
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
