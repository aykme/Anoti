package com.alekseivinogradov.anime_database.room.impl.presentation.di

import com.alekseivinogradov.anime_database.room.impl.presentation.di.base.AnimeDatabaseBaseModule
import com.alekseivinogradov.anime_database.room.impl.presentation.di.store.AnimeDatabaseStoreModule
import dagger.Module

@Module(
    includes = [
        AnimeDatabaseBaseModule::class,
        AnimeDatabaseStoreModule::class,
    ]
)
interface AnimeDatabaseCompletedModule
