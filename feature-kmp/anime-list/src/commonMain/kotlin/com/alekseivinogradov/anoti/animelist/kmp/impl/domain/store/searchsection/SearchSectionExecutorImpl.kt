package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.RESTORED_SECTION_MAX_ITEM_COUNT
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.SEARCH_DEBOUNCE_MILLISECONDS
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionExecutor
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.PageLoadResult
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.Paginator
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// One function per Intent handled, not incidental growth.
@Suppress("TooManyFunctions")
class SearchSectionExecutorImpl(
    coroutineContextProvider: CoroutineContextProvider,
    private val usecases: SearchUsecases,
    private val systemMessageProvider: SystemMessageProvider
) : SearchSectionExecutor(
    mainContext = coroutineContextProvider.newMainCoroutineContext()
) {

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
            is SearchSectionStore.Intent.RestoreSection -> restoreSection(intent)
        }
    }

    private fun createPaginator(
        searchText: String = state().searchText
    ): Paginator<ListItemDomain> {
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
        if (searchFlow == null) {
            searchFlow = MutableStateFlow(state().searchText)
        }
        if (state().sectionContent.contentType != ContentTypeDomain.LOADED) {
            val restoreTargetItemCount = state().restoreTargetItemCount
            if (restoreTargetItemCount != null) {
                restoreSectionContent(restoreTargetItemCount)
            } else {
                updateSection(resetListPosition = false)
            }
        }
        subscribeToSearchTextChanges()
    }

    private fun restoreSection(intent: SearchSectionStore.Intent.RestoreSection) {
        dispatch(
            SearchSectionStore.Message.RestoreSection(
                itemCount = intent.itemCount,
                enabledExtraEpisodesInfoIds = intent.enabledExtraEpisodesInfoIds,
                nextEpisodesInfo = intent.nextEpisodesInfo
            )
        )
    }

    // Never resets enabledExtraEpisodesInfoIds/animeDetails: RestoreSection just seeded them, and
    // this call, from OpenSection, must page in enough items without disturbing that state.
    private fun restoreSectionContent(targetItemCount: Int) {
        updateSectionJob?.cancel()
        loadNextPageJob?.cancel()
        paginator = createPaginator()
        val cappedTarget = minOf(targetItemCount, RESTORED_SECTION_MAX_ITEM_COUNT)
        updateSectionJob = scope.launch {
            dispatch(SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING))
            var items = listOf<ListItemDomain>()
            var pageResult: PageLoadResult<ListItemDomain>? = paginator.loadFirstPage()
            while (pageResult is PageLoadResult.Success) {
                items = items + pageResult.items
                if (items.size >= cappedTarget || pageResult.items.isEmpty()) break
                pageResult = paginator.loadNextPage()
            }
            when (pageResult) {
                is PageLoadResult.Error -> {
                    systemMessageProvider.makeConnectionErrorSystemMessage()
                    dispatch(SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR))
                    return@launch
                }

                is PageLoadResult.UnexpectedError -> {
                    systemMessageProvider.makeUnknownErrorSystemMessage()
                    dispatch(SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR))
                    return@launch
                }

                else -> Unit
            }
            dispatch(SearchSectionStore.Message.UpdateListItems(items))
            dispatch(SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED))
            dispatch(SearchSectionStore.Message.ClearRestoreTargetItemCount)
        }
    }

    // The flow's current value is the one openSection() just loaded above, so drop(1) skips that
    // replay and only reacts to a later, genuine edit made through changeSearchText().
    @OptIn(FlowPreview::class)
    private fun subscribeToSearchTextChanges() {
        if (changeSearchJob?.isActive == true) return
        changeSearchJob = scope.launch {
            searchFlow?.drop(1)?.debounce(SEARCH_DEBOUNCE_MILLISECONDS)
                ?.collect {
                    updateSection(resetListPosition = true)
                }
        }
    }

    private fun updateSection(resetListPosition: Boolean = true) {
        updateSectionJob?.cancel()
        loadNextPageJob?.cancel()
        paginator = createPaginator()
        updateSectionJob = scope.launch {
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
            if (resetListPosition) {
                publish(SearchSectionStore.Label.ResetListPositionAfterUpdate)
            }
            when (val result = paginator.loadFirstPage()) {
                is PageLoadResult.Success -> {
                    dispatch(SearchSectionStore.Message.UpdateListItems(result.items))
                    dispatch(
                        SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                    )
                }

                is PageLoadResult.Error -> {
                    systemMessageProvider.makeConnectionErrorSystemMessage()
                    dispatch(
                        SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }

                is PageLoadResult.UnexpectedError -> {
                    systemMessageProvider.makeUnknownErrorSystemMessage()
                    dispatch(
                        SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        loadNextPageJob = scope.launch {
            when (val result = paginator.loadNextPage()) {
                is PageLoadResult.Success -> dispatch(
                    SearchSectionStore.Message.UpdateListItems(
                        state().sectionContent.listItems + result.items
                    )
                )

                is PageLoadResult.Error -> systemMessageProvider.makeConnectionErrorSystemMessage()
                is PageLoadResult.UnexpectedError -> {
                    systemMessageProvider.makeUnknownErrorSystemMessage()
                }
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
        val newEnabledExtraEpisodesInfoIds =
            state().sectionContent.enabledExtraEpisodesInfoIds - listItem.id

        dispatch(
            SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }

    private fun extraEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds =
            state().sectionContent.enabledExtraEpisodesInfoIds + listItem.id

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

    private fun updateAnimeDetails(id: AnimeId) {
        updateAnimeDetailsJobMap[id]?.cancel()
        val job = scope.launch {
            val result = usecases
                .fetchAnimeDetailsByIdUsecase
                .execute(id)

            when (result) {
                is CallResult.Success -> onSuccessUpdateAnimeDetails(
                    updateListItem = result.value
                )

                is CallResult.HttpError,
                is CallResult.NetworkError -> {
                    systemMessageProvider.makeConnectionErrorSystemMessage()
                }

                is CallResult.OtherError -> systemMessageProvider.makeUnknownErrorSystemMessage()
            }
        }
        updateAnimeDetailsJobMap[id] = job
        // Keeping the finished job would hold every id the section ever expanded for the
        // executor's whole lifetime. Removed by identity, so the job cancelled above cannot
        // evict its own replacement.
        job.invokeOnCompletion {
            if (updateAnimeDetailsJobMap[id] === job) updateAnimeDetailsJobMap.remove(id)
        }
    }

    private fun onSuccessUpdateAnimeDetails(updateListItem: ListItemDomain) {
        val newNextEpisodesInfo = state().sectionContent.animeDetails.nextEpisodesInfo +
            (updateListItem.id to updateListItem.nextEpisodeAt)

        dispatch(
            SearchSectionStore.Message.UpdateAnimeDetails(
                animeDetails = state().sectionContent.animeDetails.copy(
                    nextEpisodesInfo = newNextEpisodesInfo
                )
            )
        )
    }
}
