package com.alekseivinogradov.anime_list.impl.domain.store.announced_section

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.alekseivinogradov.anime_base.api.domain.provider.ToastProvider
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.impl.domain.paging.AnnouncedListDataSource
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.celebrity.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.celebrity.api.domain.PAGING_PREFETCH_DISTANCE
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AnnouncedSectionExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: AnnouncedUsecases,
    private val toastProvider: ToastProvider
) : AnnouncedSectionExecutor() {

    private var updateSectionJob: Job? = null

    override fun executeIntent(intent: AnnouncedSectionStore.Intent) {
        when (intent) {
            AnnouncedSectionStore.Intent.OpenSection -> openSection()
            AnnouncedSectionStore.Intent.UpdateSection -> updateSection()
            is AnnouncedSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
        }
    }

    private fun openSection() {
        publish(AnnouncedSectionStore.Label.ResetListPositionAfterUpdate)
        if (state().sectionContent.contentType != ContentTypeDomain.LOADED) {
            updateSection()
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            dispatch(
                AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            dispatch(
                AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoIds = setOf()
                )
            )
            getPagingDataFlow().collect { listItems: PagingData<ListItemDomain> ->
                dispatch(AnnouncedSectionStore.Message.UpdateListItems(listItems))
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
            AnnouncedListDataSource(
                fetchAnnouncedAnimeListUseCase = usecases.fetchAnnouncedAnimeListUsecase,
                toastProvider = toastProvider,
                initialLoadSuccessCallback = ::initialLoadSuccessCallback,
                initialLoadErrorCallback = ::initialLoadErrorCallback
            )
        }.flow.catch { toastProvider.getMakeUnknownErrorToastCallback() }
            .cachedIn(scope)
    }

    private fun initialLoadSuccessCallback() {
        dispatch(
            AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
        )
    }

    private fun initialLoadErrorCallback() {
        dispatch(
            AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
        )
    }

    private fun episodeInfoClick(intent: AnnouncedSectionStore.Intent.EpisodesInfoClick) {
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
