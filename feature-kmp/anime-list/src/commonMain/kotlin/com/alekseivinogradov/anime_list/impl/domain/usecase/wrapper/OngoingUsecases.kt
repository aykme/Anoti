package com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchOngoingAnimeListUsecase

data class OngoingUsecases(
    val fetchOngoingAnimeListUsecase: FetchOngoingAnimeListUsecase,
    val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase
)
