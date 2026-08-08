package com.alekseivinogradov.anoti.animelist.kmp.api.presentation

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

interface AnimeListView : MviView<UiModel, AnimeListMainStore.Intent>
