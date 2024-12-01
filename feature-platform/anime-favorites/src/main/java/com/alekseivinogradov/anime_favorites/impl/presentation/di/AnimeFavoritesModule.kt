package com.alekseivinogradov.anime_favorites.impl.presentation.di

import com.alekseivinogradov.anime_background_update.impl.presentation.di.AnimeOnceBackgroundUpdateModule
import com.alekseivinogradov.anime_base.impl.presentation.di.AnimeBaseModule
import com.alekseivinogradov.network.impl.presentation.di.NetworkModule
import dagger.Module

@Module(
    includes = [
        AnimeBaseModule::class,
        NetworkModule::class,
        AnimeOnceBackgroundUpdateModule::class
    ]
)
interface AnimeFavoritesModule
