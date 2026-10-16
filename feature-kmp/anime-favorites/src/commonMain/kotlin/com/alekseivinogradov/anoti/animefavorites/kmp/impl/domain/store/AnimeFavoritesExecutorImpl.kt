package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.ANIMATION_DURATION_SHORT
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.LIST_ARRIVAL_TIMEOUT_SECONDS
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesExecutor
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

// One function per Intent handled, not incidental growth.
@Suppress("TooManyFunctions")
class AnimeFavoritesExecutorImpl(
    coroutineContextProvider: CoroutineContextProvider,
    private val usecases: FavoritesUsecases
) : AnimeFavoritesExecutor(
    mainContext = coroutineContextProvider.newMainCoroutineContext()
) {

    private var updateListItemsJob: Job? = null
    private var updateSectionJob: Job? = null

    // Signals a fresh UpdateListItems intent arriving during an open/refresh cycle, so the
    // minimum-duration resolve below waits for real data instead of judging a stale snapshot.
    private var listItemsArrivedSignal: CompletableDeferred<Unit>? = null

    override fun executeIntent(intent: AnimeFavoritesMainStore.Intent) {
        when (intent) {
            is AnimeFavoritesMainStore.Intent.UpdateListItems -> updateListItems(intent)
            is AnimeFavoritesMainStore.Intent.ItemsSubmittedToList -> itemsSubmittedToList()
            AnimeFavoritesMainStore.Intent.OpenSection -> openSection()
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
        // Dispatched synchronously, before signaling arrival below: a resolve waiting on that
        // signal must see the fresh list.listItems the moment it wakes up, not a stale one.
        dispatch(AnimeFavoritesMainStore.Message.UpdateListItems(intent.listItems))
        listItemsArrivedSignal?.complete(Unit)

        val contentType = state().contentType
        // An open/refresh cycle already in progress resolves EMPTY vs LOADED itself, once its
        // own minimum duration and this same list arrival have both happened.
        val isResolvingSectionLoad =
            contentType is ContentTypeDomain.LOADING && contentType.hasMinimumDuration
        if (intent.listItems.isEmpty() &&
            contentType != ContentTypeDomain.EMPTY &&
            !isResolvingSectionLoad
        ) {
            updateListItemsJob = scope.launch {
                dispatch(
                    AnimeFavoritesMainStore.Message.ChangeContentType(
                        ContentTypeDomain.LOADING()
                    )
                )
                delay(ANIMATION_DURATION_SHORT)
                println("$ANOTI_TAG AnimeFavorites: favorites shown: none")
                dispatch(
                    AnimeFavoritesMainStore.Message.ChangeContentType(
                        ContentTypeDomain.EMPTY
                    )
                )
            }
        }
    }

    private fun itemsSubmittedToList() {
        val contentType = state().contentType
        if (contentType is ContentTypeDomain.LOADING && contentType.hasMinimumDuration) return
        if (contentType != ContentTypeDomain.LOADED) {
            println("$ANOTI_TAG AnimeFavorites: favorites shown: ${state().listItems.size}")
            dispatch(AnimeFavoritesMainStore.Message.ChangeContentType(ContentTypeDomain.LOADED))
        }
    }

    // The section became selected: not a data refresh, just shows loading for the same
    // minimum duration as updateSection() so opening the screen doesn't flash faster than a
    // manual refresh does. The database reset itself is triggered by the caller directly
    // (see NavAnimeFavoritesScreenComponent) rather than through a Label here: that label would
    // only reach AnimeDatabaseStore once AnimeFavoritesController's binder has attached, which
    // isn't guaranteed yet the moment the screen mounts and this runs.
    private fun openSection() {
        updateListItemsJob?.cancel()
        dispatch(
            AnimeFavoritesMainStore.Message.ChangeContentType(
                ContentTypeDomain.LOADING(hasMinimumDuration = true)
            )
        )
        resolveContentTypeAfterMinimumDuration()
    }

    private fun updateSection() {
        // A pending empty-list transition from a stale updateListItems() call must not be left
        // to fire later and unconditionally overwrite this refresh's own resolution.
        updateListItemsJob?.cancel()
        dispatch(
            AnimeFavoritesMainStore.Message.ChangeContentType(
                ContentTypeDomain.LOADING(hasMinimumDuration = true)
            )
        )
        publish(AnimeFavoritesMainStore.Label.ResetExtraInfo)
        publish(AnimeFavoritesMainStore.Label.UpdateSection)
        resolveContentTypeAfterMinimumDuration()
    }

    private fun resolveContentTypeAfterMinimumDuration() {
        updateSectionJob?.cancel()
        val listItemsArrived = CompletableDeferred<Unit>()
        listItemsArrivedSignal = listItemsArrived
        updateSectionJob = scope.launch {
            delay(ANIMATION_DURATION_SHORT)
            // Waits for the minimum duration AND a fresh list, whichever finishes later — a
            // slow database read must not resolve against the stale list.listItems from before
            // this cycle started. The timeout is the backstop: an answer that never comes would
            // otherwise leave the screen loading for good.
            val arrived =
                withTimeoutOrNull(LIST_ARRIVAL_TIMEOUT_SECONDS) { listItemsArrived.await() }
            if (arrived == null) {
                println("$ANOTI_TAG AnimeFavorites: the list did not arrive in time")
            }
            val contentType = state().contentType
            if (contentType is ContentTypeDomain.LOADING && contentType.hasMinimumDuration) {
                val finalContentType = if (state().listItems.isEmpty()) {
                    ContentTypeDomain.EMPTY
                } else {
                    ContentTypeDomain.LOADED
                }
                val favoritesCount = state().listItems.size
                println(
                    "$ANOTI_TAG AnimeFavorites: favorites shown: " +
                        if (favoritesCount == 0) "none" else "$favoritesCount"
                )
                dispatch(AnimeFavoritesMainStore.Message.ChangeContentType(finalContentType))
            }
        }
    }

    private fun updateAllItemsInBackground() {
        println("$ANOTI_TAG AnimeFavorites: a refresh of every favorite is requested")
        usecases.updateAllAnimeInBackgroundOnceUsecase.execute()
    }

    private fun itemClick(intent: AnimeFavoritesMainStore.Intent.ItemClick) {
        publish(AnimeFavoritesMainStore.Label.ItemClick(intent.id))
    }

    private fun infoTypeClick(intent: AnimeFavoritesMainStore.Intent.InfoTypeClick) {
        val listItem = state().listItems.find { listItemDomain: ListItemDomain ->
            listItemDomain.id == intent.id
        } ?: return

        if (listItem.isExtraInfoEnabled) {
            changeInfoTypeToMain(listItem)
        } else {
            changeInfoTypeToExtra(listItem)
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

    private fun changeInfoTypeToMain(listItem: ListItemDomain) {
        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = listItem.copy(isExtraInfoEnabled = false)
            )
        )
    }

    private fun changeInfoTypeToExtra(listItem: ListItemDomain) {
        publish(
            AnimeFavoritesMainStore.Label.UpdateListItem(
                listItem = listItem.copy(isExtraInfoEnabled = true)
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
