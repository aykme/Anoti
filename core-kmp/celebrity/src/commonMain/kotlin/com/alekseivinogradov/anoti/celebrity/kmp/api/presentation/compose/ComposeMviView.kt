package com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.MviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer

/**
 * Renders an MVIKotlin store's state as Compose [State] instead of an imperative `render()` call
 * to a real View — the shared piece every Compose-hosted screen's [MviView] implementation reuses.
 *
 * Delegates event dispatch/subscription to [BaseMviView], the same plumbing every classic
 * [MviView] implementation in this project uses, and only replaces its renderer with one that
 * writes into [model] instead of touching real View widgets.
 *
 * @param UiModel the store's presentation-ready state type.
 * @param Intent the store's intent type, dispatched back via [dispatch].
 * @param initialModel a value [model] holds before the first [render] call — read straight from
 * the store's own synchronous [com.arkivanov.mvikotlin.core.store.Store.state] where a caller
 * needs the first composition to already reflect real state, instead of waiting for the
 * store/view binding (which completes asynchronously, on a later main-thread dispatch).
 */
abstract class ComposeMviView<UiModel : Any, Intent : Any>(
    initialModel: UiModel? = null
) : BaseMviView<UiModel, Intent>() {

    private val internalModel = mutableStateOf(initialModel)

    /** The latest rendered model, or `null` before the first [render] call. */
    val model: State<UiModel?> = internalModel

    final override val renderer: ViewRenderer<UiModel> =
        object : ViewRenderer<UiModel> {
            override fun render(model: UiModel) {
                internalModel.value = model
            }
        }
}
