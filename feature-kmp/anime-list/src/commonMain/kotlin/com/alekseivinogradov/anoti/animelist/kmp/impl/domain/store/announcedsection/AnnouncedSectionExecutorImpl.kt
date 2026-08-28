package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionExecutor
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.PageLoadResult
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging.Paginator
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AnnouncedSectionExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: AnnouncedUsecases,
    private val toastProvider: ToastProvider
) : AnnouncedSectionExecutor() {

    private var updateSectionJob: Job? = null
    private var loadNextPageJob: Job? = null
    private var paginator: Paginator<ListItemDomain> = createPaginator()

    override fun executeIntent(intent: AnnouncedSectionStore.Intent) {
        when (intent) {
            AnnouncedSectionStore.Intent.OpenSection -> openSection()
            AnnouncedSectionStore.Intent.UpdateSection -> updateSection()
            AnnouncedSectionStore.Intent.LoadNextPage -> loadNextPage()
            is AnnouncedSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
        }
    }

    private fun createPaginator(): Paginator<ListItemDomain> {
        return Paginator(
            firstPage = FIRST_PAGE,
            loadPage = { page -> usecases.fetchAnnouncedAnimeListUsecase.execute(page = page) }
        )
    }

    private fun openSection() {
        publish(AnnouncedSectionStore.Label.ResetListPositionAfterUpdate)
        if (state().sectionContent.contentType != ContentTypeDomain.LOADED) {
            updateSection()
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        loadNextPageJob?.cancel()
        paginator = createPaginator()
        updateSectionJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
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
                    toastProvider.makeConnectionErrorToast()
                    dispatch(
                        AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }

                is PageLoadResult.UnexpectedError -> {
                    toastProvider.makeUnknownErrorToast()
                    dispatch(
                        AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                    )
                }
            }
        }
    }

    private fun loadNextPage() {
        loadNextPageJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            when (val result = paginator.loadNextPage()) {
                is PageLoadResult.Success -> dispatch(
                    AnnouncedSectionStore.Message.UpdateListItems(
                        state().sectionContent.listItems + result.items
                    )
                )

                is PageLoadResult.Error -> toastProvider.makeConnectionErrorToast()
                is PageLoadResult.UnexpectedError -> toastProvider.makeUnknownErrorToast()
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
