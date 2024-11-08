package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_base.api.domain.provider.ToastProvider
import com.alekseivinogradov.anime_list.api.domain.model.AnimeDetails
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.paging.OngoingListDataSource
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.celebrity.api.domain.PAGING_PREFETCH_DISTANCE
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class OngoingSectionExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: OngoingUsecases,
    private val toastProvider: ToastProvider
) : OngoingSectionExecutor() {

    private var updateSectionJob: Job? = null
    private val updateAnimeDetailsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()

    override fun executeAction(action: OngoingSectionStore.Action) {
        when (action) {
            OngoingSectionStore.Action.InitSection -> openSection()
        }
    }

    override fun executeIntent(intent: OngoingSectionStore.Intent) {
        when (intent) {
            OngoingSectionStore.Intent.OpenSection -> openSection()
            OngoingSectionStore.Intent.UpdateSection -> updateSection()
            is OngoingSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
        }
    }

    private fun openSection() {
        publish(OngoingSectionStore.Label.ResetListPositionAfterUpdate)
        if (state().sectionContent.contentType != ContentTypeDomain.LOADED) {
            updateSection()
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
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
            getPagingDataFlow().collect { listItems: PagingData<ListItemDomain> ->
                dispatch(OngoingSectionStore.Message.UpdateListItems(listItems))
            }
        }
    }

    private fun getPagingDataFlow(): Flow<PagingData<ListItemDomain>> {
        return Pager(
            config = PagingConfig(
                pageSize = ITEMS_PER_PAGE,
                prefetchDistance = PAGING_PREFETCH_DISTANCE,
                enablePlaceholders = true
            ),

            ) {
            OngoingListDataSource(
                fetchOngoingAnimeListUseCase = usecases.fetchOngoingAnimeListUsecase,
                toastProvider = toastProvider,
                initialLoadSuccessCallback = ::initialLoadSuccessCallback,
                initialLoadErrorCallback = ::initialLoadErrorCallback
            )
        }.flow.catch { toastProvider.getMakeUnknownErrorToastCallback() }
            .cachedIn(scope)
    }

    private fun initialLoadSuccessCallback() {
        dispatch(
            OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
        )
    }

    private fun initialLoadErrorCallback() {
        dispatch(
            OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
        )
    }

    private fun episodeInfoClick(intent: OngoingSectionStore.Intent.EpisodesInfoClick) {
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
            OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
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
        updateAnimeDetailsJobMap[id] = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            val result = usecases
                .fetchAnimeDetailsByIdUsecase
                .execute(id)

            when (result) {
                is CallResult.Success -> onSuccessUpdateAnimeDetails(
                    updateListItem = result.value
                )

                is CallResult.HttpError,
                is CallResult.OtherError -> toastProvider.getMakeConnectionErrorToastCallback()
            }
        }
    }

    private fun onSuccessUpdateAnimeDetails(
        updateListItem: ListItemDomain
    ) {
        val newNextEpisodesInfo = state()
            .sectionContent
            .animeDetails
            .nextEpisodesInfo
            .toMutableMap().apply {
                this[updateListItem.id] = updateListItem.nextEpisodeAt
            }

        dispatch(
            OngoingSectionStore.Message.UpdateAnimeDetails(
                animeDetails = state().sectionContent.animeDetails.copy(
                    nextEpisodesInfo = newNextEpisodesInfo
                )
            )
        )
    }
}
