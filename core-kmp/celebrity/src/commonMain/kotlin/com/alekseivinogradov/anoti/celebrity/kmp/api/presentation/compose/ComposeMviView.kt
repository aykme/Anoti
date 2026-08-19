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
 */
abstract class ComposeMviView<UiModel : Any, Intent : Any> : BaseMviView<UiModel, Intent>() {

    private val internalModel = mutableStateOf<UiModel?>(null)

    /** The latest rendered model, or `null` before the first [render] call. */
    val model: State<UiModel?> = internalModel

    final override val renderer: ViewRenderer<UiModel> =
        object : ViewRenderer<UiModel> {
            override fun render(model: UiModel) {
                internalModel.value = model
            }
        }
}
