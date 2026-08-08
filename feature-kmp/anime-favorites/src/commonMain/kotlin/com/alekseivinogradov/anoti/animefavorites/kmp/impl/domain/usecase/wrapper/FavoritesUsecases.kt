package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase

data class FavoritesUsecases(
    val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase,
    val fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
)
