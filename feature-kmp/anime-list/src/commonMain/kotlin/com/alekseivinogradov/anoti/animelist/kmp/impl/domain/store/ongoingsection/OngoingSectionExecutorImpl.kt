package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.RESTORED_SECTION_MAX_ITEM_COUNT
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionExecutor
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.PageLoadResult
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.Paginator
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// One function per Intent handled, not incidental growth.
@Suppress("TooManyFunctions")
class OngoingSectionExecutorImpl(
    coroutineContextProvider: CoroutineContextProvider,
    private val usecases: OngoingUsecases,
    private val systemMessageProvider: SystemMessageProvider
) : OngoingSectionExecutor(
    mainContext = coroutineContextProvider.newMainCoroutineContext()
) {

    private var updateSectionJob: Job? = null
    private var loadNextPageJob: Job? = null
    private val updateAnimeDetailsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private var paginator: Paginator<ListItemDomain> = createPaginator()

    override fun executeIntent(intent: OngoingSectionStore.Intent) {
        when (intent) {
            OngoingSectionStore.Intent.OpenSection -> openSection()
            OngoingSectionStore.Intent.UpdateSection -> updateSection()
            OngoingSectionStore.Intent.LoadNextPage -> loadNextPage()
            is OngoingSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
            is OngoingSectionStore.Intent.RestoreSection -> restoreSection(intent)
        }
    }

    private fun createPaginator(): Paginator<ListItemDomain> {
        return Paginator(
            firstPage = FIRST_PAGE,
            loadPage = { page -> usecases.fetchOngoingAnimeListUsecase.execute(page = page) }
        )
    }

    private fun openSection() {
        if (state().sectionContent.contentType != ContentTypeDomain.LOADED) {
            val restoreTargetItemCount = state().restoreTargetItemCount
            if (restoreTargetItemCount != null) {
                restoreSectionContent(restoreTargetItemCount)
            } else {
                updateSection()
            }
        }
    }

    private fun restoreSection(intent: OngoingSectionStore.Intent.RestoreSection) {
        dispatch(
            OngoingSectionStore.Message.RestoreSection(
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
            dispatch(OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING))
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
                    dispatch(OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR))
                    return@launch
                }

                is PageLoadResult.UnexpectedError -> {
                    systemMessageProvider.makeUnknownErrorSystemMessage()
                    dispatch(OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR))
                    return@launch
                }

                else -> Unit
            }
            dispatch(OngoingSectionStore.Message.UpdateListItems(items))
            dispatch(OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED))
            dispatch(OngoingSectionStore.Message.ClearRestoreTargetItemCount)
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        loadNextPageJob?.cancel()
        paginator = createPaginator()
        updateSectionJob = scope.launch {
            dispatch(
                OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            dispatch(
                OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoIds = setOf()
                )
            )
            dispatch(
                OngoingSectionStore.Message.UpdateAnimeDetails(
                    animeDetails = AnimeDetails()
                )
            )
            when (val result = paginator.loadFirstPage()) {
                is PageLoadResult.Success -> {
                    dispatch(OngoingSectionStore.Message.UpdateListItems(result.items))
                    dispatch(
                        OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                    )
                }

                is PageLoadResult.Error -> {
                    systemMessageProvider.makeConnectionErrorSystemMessage()
                    dispatch(
                        OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }

                is PageLoadResult.UnexpectedError -> {
                    systemMessageProvider.makeUnknownErrorSystemMessage()
                    dispatch(
                        OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        // A page already on its way keeps this slot. Overwriting it would leave that load
        // untracked, and a later refresh could then no longer cancel it.
        if (loadNextPageJob?.isActive == true) return

        loadNextPageJob = scope.launch {
            when (val result = paginator.loadNextPage()) {
                is PageLoadResult.Success -> dispatch(
                    OngoingSectionStore.Message.UpdateListItems(
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

    private fun episodeInfoClick(intent: OngoingSectionStore.Intent.EpisodesInfoClick) {
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
            OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }

    private fun extraEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds =
            state().sectionContent.enabledExtraEpisodesInfoIds + listItem.id

        dispatch(
            OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
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
        // executor's whole lifetime. Removed by identity, so the job canceled above cannot
        // evict its own replacement.
        job.invokeOnCompletion {
            if (updateAnimeDetailsJobMap[id] === job) updateAnimeDetailsJobMap.remove(id)
        }
    }

    private fun onSuccessUpdateAnimeDetails(
        updateListItem: ListItemDomain
    ) {
        val newNextEpisodesInfo = state().sectionContent.animeDetails.nextEpisodesInfo +
            (updateListItem.id to updateListItem.nextEpisodeAt)

        dispatch(
            OngoingSectionStore.Message.UpdateAnimeDetails(
                animeDetails = state().sectionContent.animeDetails.copy(
                    nextEpisodesInfo = newNextEpisodesInfo
                )
            )
        )
    }
}
