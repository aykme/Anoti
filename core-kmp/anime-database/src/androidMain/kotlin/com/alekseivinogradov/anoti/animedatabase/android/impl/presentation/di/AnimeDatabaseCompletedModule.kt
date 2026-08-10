package com.alekseivinogradov.anoti.animedatabase.android.impl.presentation.di

import com.alekseivinogradov.anoti.animedatabase.android.impl.presentation.di.base.AnimeDatabaseBaseModule
import com.alekseivinogradov.anoti.animedatabase.android.impl.presentation.di.store.AnimeDatabaseStoreModule
import dagger.Module

@Module(
    includes = [
        AnimeDatabaseBaseModule::class,
        AnimeDatabaseStoreModule::class,
    ]
)
interface AnimeDatabaseCompletedModule
