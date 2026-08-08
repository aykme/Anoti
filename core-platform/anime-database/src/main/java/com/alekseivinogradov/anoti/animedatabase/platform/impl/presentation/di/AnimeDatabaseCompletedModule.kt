package com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di

import com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di.base.AnimeDatabaseBaseModule
import com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di.store.AnimeDatabaseStoreModule
import dagger.Module

@Module(
    includes = [
        AnimeDatabaseBaseModule::class,
        AnimeDatabaseStoreModule::class,
    ]
)
interface AnimeDatabaseCompletedModule
