package com.alekseivinogradov.anoti.animelist.kmp.api.presentation

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

/**
 * The view contract the platform layer implements to render the main store's state.
 */
interface AnimeListView : MviView<UiModel, AnimeListMainStore.Intent>
