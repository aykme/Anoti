package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.RESTORED_SECTION_MAX_ITEM_COUNT
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionExecutor
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.PageLoadResult
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.Paginator
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AnnouncedSectionExecutorImpl(
    coroutineContextProvider: CoroutineContextProvider,
    private val usecases: AnnouncedUsecases,
    private val systemMessageProvider: SystemMessageProvider
) : AnnouncedSectionExecutor(
    mainContext = coroutineContextProvider.newMainCoroutineContext()
) {

    private var updateSectionJob: Job? = null
    private var loadNextPageJob: Job? = null
    private var paginator: Paginator<ListItemDomain> = createPaginator()

    override fun executeIntent(intent: AnnouncedSectionStore.Intent) {
        when (intent) {
            AnnouncedSectionStore.Intent.OpenSection -> openSection()
            AnnouncedSectionStore.Intent.UpdateSection -> updateSection()
            AnnouncedSectionStore.Intent.LoadNextPage -> loadNextPage()
            is AnnouncedSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
            is AnnouncedSectionStore.Intent.RestoreSection -> restoreSection(intent)
        }
    }

    private fun createPaginator(): Paginator<ListItemDomain> {
        return Paginator(
            firstPage = FIRST_PAGE,
            loadPage = { page -> usecases.fetchAnnouncedAnimeListUsecase.execute(page = page) }
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

    private fun restoreSection(intent: AnnouncedSectionStore.Intent.RestoreSection) {
        dispatch(
            AnnouncedSectionStore.Message.RestoreSection(
                itemCount = intent.itemCount,
                enabledExtraEpisodesInfoIds = intent.enabledExtraEpisodesInfoIds
            )
        )
    }

    // Never resets enabledExtraEpisodesInfoIds: RestoreSection just seeded it, and this call,
    // from OpenSection, must page in enough items without disturbing that state.
    private fun restoreSectionContent(targetItemCount: Int) {
        updateSectionJob?.cancel()
        loadNextPageJob?.cancel()
        paginator = createPaginator()
        val cappedTarget = minOf(targetItemCount, RESTORED_SECTION_MAX_ITEM_COUNT)
        updateSectionJob = scope.launch {
            dispatch(AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING))
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
                    dispatch(AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR))
                    return@launch
                }

                is PageLoadResult.UnexpectedError -> {
                    systemMessageProvider.makeUnknownErrorSystemMessage()
                    dispatch(AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR))
                    return@launch
                }

                else -> Unit
            }
            dispatch(AnnouncedSectionStore.Message.UpdateListItems(items))
            dispatch(AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED))
            dispatch(AnnouncedSectionStore.Message.ClearRestoreTargetItemCount)
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        loadNextPageJob?.cancel()
        paginator = createPaginator()
        updateSectionJob = scope.launch {
            dispatch(
                AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            dispatch(
                AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoIds = setOf()
                )
            )
            when (val result = paginator.loadFirstPage()) {
                is PageLoadResult.Success -> {
                    dispatch(AnnouncedSectionStore.Message.UpdateListItems(result.items))
                    dispatch(
                        AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                    )
                }

                is PageLoadResult.Error -> {
                    systemMessageProvider.makeConnectionErrorSystemMessage()
                    dispatch(
                        AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }

                is PageLoadResult.UnexpectedError -> {
                    systemMessageProvider.makeUnknownErrorSystemMessage()
                    dispatch(
                        AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        loadNextPageJob = scope.launch {
            when (val result = paginator.loadNextPage()) {
                is PageLoadResult.Success -> dispatch(
                    AnnouncedSectionStore.Message.UpdateListItems(
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

    private fun episodeInfoClick(intent: AnnouncedSectionStore.Intent.EpisodesInfoClick) {
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
            AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
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
            AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }
}
