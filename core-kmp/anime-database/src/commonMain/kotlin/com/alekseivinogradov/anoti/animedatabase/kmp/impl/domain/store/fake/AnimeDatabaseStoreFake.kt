package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.fake

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.rx.Disposable
import com.arkivanov.mvikotlin.core.rx.Observer

/**
 * The saved-anime store backed by a list in memory. It answers every intent by changing that
 * list, and [emit] replaces it outright for a test that would rather set the state than reach it.
 *
 * An intent naming an id the list does not hold is dropped without emitting. So is one that
 * would change nothing. Both rules match the real store.
 *
 * Two things here are not the real store. The state changes inside [accept] instead of waiting
 * for the database to emit again, and a reset arriving while another is still running is
 * applied rather than dropped.
 *
 * @param initialItems the saved anime the store starts with.
 */
class AnimeDatabaseStoreFake(
    initialItems: List<AnimeDbDomain> = listOf()
) : AnimeDatabaseStore {

    private val stateObservers = mutableListOf<Observer<AnimeDatabaseStore.State>>()

    private val labelObservers = mutableListOf<Observer<AnimeDatabaseStore.Label>>()

    override var state = AnimeDatabaseStore.State(animeDatabaseItems = initialItems)
        private set

    override var isDisposed = false
        private set

    override fun init() = Unit

    override fun accept(intent: AnimeDatabaseStore.Intent) {
        val items = state.animeDatabaseItems
        when (intent) {
            is AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem ->
                if (!items.holds(intent.animeDatabaseItem.id)) {
                    emit(items + intent.animeDatabaseItem)
                }

            is AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem ->
                if (items.holds(intent.id)) {
                    emit(items.filterNot { it.id == intent.id })
                }

            is AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem ->
                if (items.holds(intent.animeDatabaseItem.id)) {
                    emit(
                        items.map { item: AnimeDbDomain ->
                            if (item.id == intent.animeDatabaseItem.id) {
                                intent.animeDatabaseItem
                            } else {
                                item
                            }
                        }
                    )
                }

            // The mark only ever comes off: an item that does not carry it is left alone.
            is AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus ->
                if (items.any { it.id == intent.id && it.isNewEpisode }) {
                    emit(
                        items.map { item: AnimeDbDomain ->
                            if (item.id == intent.id) {
                                item.copy(isNewEpisode = intent.isNewEpisode)
                            } else {
                                item
                            }
                        }
                    )
                }

            AnimeDatabaseStore.Intent.ResetAllItemsNewEpisodeStatus -> {
                emit(items.map { it.copy(isNewEpisode = false) })
                publish(AnimeDatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished)
            }

            AnimeDatabaseStore.Intent.ResetAllItemsExtraInfo ->
                emit(items.map { it.copy(isExtraInfoEnabled = false, nextEpisodeAt = null) })
        }
    }

    override fun states(observer: Observer<AnimeDatabaseStore.State>): Disposable {
        observer.onNext(state)
        stateObservers += observer
        return Disposable { stateObservers -= observer }
    }

    override fun labels(observer: Observer<AnimeDatabaseStore.Label>): Disposable {
        labelObservers += observer
        return Disposable { labelObservers -= observer }
    }

    // A disposed store completes both streams and keeps nobody subscribed, same as a real one.
    override fun dispose() {
        isDisposed = true
        stateObservers.toList().forEach(Observer<AnimeDatabaseStore.State>::onComplete)
        labelObservers.toList().forEach(Observer<AnimeDatabaseStore.Label>::onComplete)
        stateObservers.clear()
        labelObservers.clear()
    }

    /** Publishes [items] as the new saved-anime list to everyone subscribed. */
    fun emit(items: List<AnimeDbDomain>) {
        state = AnimeDatabaseStore.State(animeDatabaseItems = items)
        stateObservers.toList().forEach { it.onNext(state) }
    }

    private fun publish(label: AnimeDatabaseStore.Label) {
        labelObservers.toList().forEach { it.onNext(label) }
    }

    private fun List<AnimeDbDomain>.holds(id: AnimeId): Boolean = any { it.id == id }
}
