package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.domain.usecase.FetchAnimeListUsecase

data class Usecases(
    val fetchAnimeOngoingListUsecase: FetchAnimeListUsecase,
    val fetchAnimeAnnouncedListUsecase: FetchAnimeListUsecase,
    val fetchAnimeListBySearchUsecase: FetchAnimeListUsecase,
)