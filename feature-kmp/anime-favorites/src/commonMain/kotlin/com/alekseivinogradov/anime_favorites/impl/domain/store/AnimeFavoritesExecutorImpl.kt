package com.alekseivinogradov.anime_favorites.impl.domain.store

import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_favorites.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesExecutor
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.celebrity.api.domain.ANIMATION_DURATION_SHORT
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnimeFavoritesExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: FavoritesUsecases,
    private var toastProvider: ToastProvider
) : AnimeFavoritesExecutor() {

    private var updateListItemsJob: Job? = null
    private val updateAnimeDetailsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private val changeEpisodesViewedJobMap: MutableMap<AnimeId, Job> = mutableMapOf()

    override fun executeIntent(intent: AnimeFavoritesMainStore.Intent) {
        when (intent) {
            is AnimeFavoritesMainStore.Intent.UpdateListItems -> updateListItems(intent)
            is AnimeFavoritesMainStore.Intent.ItemsSubmittedToList -> itemsSubmittedToList()
            AnimeFavoritesMainStore.Intent.UpdateSection -> updateSection()
            AnimeFavoritesMainStore.Intent.UpdateAllItemsInBackground -> {
                updateAllItemsInBackground()
            }

            is AnimeFavoritesMainStore.Intent.ItemClick -> itemClick(intent)
            is AnimeFavoritesMainStore.Intent.InfoTypeClick -> infoTypeClick(intent)
            is AnimeFavoritesMainStore.Intent.NotificationClick -> notificationClick(intent)
            is AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick -> {
                episodesViewedMinusClick(intent)
            }

            is AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick -> {
                episodesViewedPlusClick(intent)
            }
        }
    }

    private fun updateListItems(intent: AnimeFavoritesMainStore.Intent.UpdateListItems) {
        updateListItemsJob?.cancel()
        updateListItemsJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            dispatch(AnimeFavoritesMainStore.Message.UpdateListItems(intent.listItems))
            if (intent.listItems.isEmpty() && state().contentType != ContentTypeDomain.EMPTY) {
                dispatch(
                    AnimeFavoritesMainStore.Message.ChangeContentType(
                        ContentTypeDomain.LOADING
                    )
                )
                delay(ANIMATION_DURATION_SHORT)
                dispatch(
                    AnimeFavoritesMainStore.Message.ChangeContentType(
                        ContentTypeDomain.EMPTY
                    )
                )
            }
        }
    }

    private fun itemsSubmittedToList() {
        dispatch(AnimeFavoritesMainStore.Message.ChangeContentType(ContentTypeDomain.LOADED))
    }

    private fun updateSection() {
        dispatch(
            AnimeFavoritesMainStore.Message.UpdateFetchedAnimeDetailsIds(
                fetchedAnimeDetailsIds = setOf()
            )
        )
        publish(AnimeFavoritesMainStore.Label.UpdateSection)
    }

    private fun updateAllItemsInBackground() {
        usecases.updateAllAnimeInBackgroundOnceUsecase.execute()
    }

    private fun itemClick(intent: AnimeFavoritesMainStore.Intent.ItemClick) {
        publish(AnimeFavoritesMainStore.Label.ItemClick(intent.id))
    }

    private fun infoTypeClick(intent: AnimeFavoritesMainStore.Intent.InfoTypeClick) {
        if (state().enabledExtraInfoIds.contains(intent.id)) {
            changeInfoTypeToMain(intent.id)
        } else {
            changeInfoTypeToExtra(intent.id)
        }
    }

    private fun notificationClick(intent: AnimeFavoritesMainStore.Intent.NotificationClick) {
        publish(AnimeFavoritesMainStore.Label.DisableNotificationClick(intent.id))
    }

    private fun episodesViewedMinusClick(
        intent: AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick
    ) {
        val listItem = state().listItems
            .find { listItemDomain: ListItemDomain ->
                listItemDomain.id == intent.id
            } ?: return

        if (listItem.episodesViewed <= 0) return

        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = listItem.copy(
                    episodesViewed = listItem.episodesViewed - 1
                )
            )
        )
    }

    private fun episodesViewedPlusClick(
        intent: AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick
    ) {
        val listItem = state().listItems
            .find { listItemDomain: ListItemDomain ->
                listItemDomain.id == intent.id
            } ?: return

        if (listItem.episodesViewed >= getMaxEpisodesViewedNumber(listItem)) return

        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = listItem.copy(
                    episodesViewed = listItem.episodesViewed + 1
                )
            )
        )
    }

    private fun changeInfoTypeToMain(id: AnimeId) {
        val newEnabledExtraInfoIds = state().enabledExtraInfoIds
            .toMutableSet().apply {
                remove(id)
            }.toSet()

        dispatch(AnimeFavoritesMainStore.Message.UpdateEnabledExtraInfoIds(newEnabledExtraInfoIds))
    }

    private fun changeInfoTypeToExtra(id: AnimeId) {
        val newEnabledExtraInfoIds = state().enabledExtraInfoIds
            .toMutableSet().apply {
                add(id)
            }.toSet()

        dispatch(AnimeFavoritesMainStore.Message.UpdateEnabledExtraInfoIds(newEnabledExtraInfoIds))

        val state = state()
        val listItem = state.listItems.find { listItemDomain: ListItemDomain ->
            listItemDomain.id == id
        } ?: return

        val isOngoingStatus = listItem.releaseStatus == ReleaseStatusDomain.ONGOING

        if (isOngoingStatus && !state.fetchedAnimeDetailsIds.contains(id)) {
            updateAnimeDetails(id)
        }
    }

    private fun updateAnimeDetails(id: AnimeId) {
        updateAnimeDetailsJobMap[id]?.cancel()
        updateAnimeDetailsJobMap[id] =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                val result = usecases
                    .fetchAnimeDetailsByIdUsecase
                    .execute(id)

                when (result) {
                    is CallResult.Success -> onSuccessUpdateAnimeDetails(
                        currentItemId = id,
                        updateListItem = result.value
                    )

                    is CallResult.HttpError,
                    is CallResult.OtherError -> toastProvider.makeConnectionErrorToast()
                }
            }
    }

    private fun onSuccessUpdateAnimeDetails(
        currentItemId: AnimeId,
        updateListItem: ListItemDomain
    ) {
        val newFetchedItemDetailsIds = state().fetchedAnimeDetailsIds
            .toMutableSet().apply {
                add(currentItemId)
            }.toSet()

        val currentListItem = state().listItems.find { listItemDomain: ListItemDomain ->
            listItemDomain.id == currentItemId
        } ?: return

        dispatch(
            AnimeFavoritesMainStore.Message.UpdateFetchedAnimeDetailsIds(
                fetchedAnimeDetailsIds = newFetchedItemDetailsIds
            )
        )
        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = currentListItem.copy(
                    nextEpisodeAt = updateListItem.nextEpisodeAt
                )
            )
        )
    }

    private fun getMaxEpisodesViewedNumber(listItem: ListItemDomain): Int {
        return when (listItem.releaseStatus) {
            ReleaseStatusDomain.ONGOING -> listItem.episodesAired ?: 0
            ReleaseStatusDomain.ANNOUNCED -> 0
            ReleaseStatusDomain.RELEASED -> listItem.episodesTotal ?: 0
            ReleaseStatusDomain.UNKNOWN -> 0
        }
    }
}
