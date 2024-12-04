package com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_background_update.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.FetchAnimeDetailsByIdUsecase

data class FavoritesUsecases(
    val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase,
    val fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
)
