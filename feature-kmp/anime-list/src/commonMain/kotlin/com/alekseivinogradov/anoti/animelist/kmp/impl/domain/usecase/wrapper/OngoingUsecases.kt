package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper

import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchOngoingAnimeListUsecase

data class OngoingUsecases(
    val fetchOngoingAnimeListUsecase: FetchOngoingAnimeListUsecase,
    val fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
)
