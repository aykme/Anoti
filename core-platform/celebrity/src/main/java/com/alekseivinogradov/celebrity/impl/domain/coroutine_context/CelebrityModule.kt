package com.alekseivinogradov.celebrity.impl.domain.coroutine_context

import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import dagger.Binds
import dagger.Module

@Module
interface CelebrityModule {
    @Binds
    fun bindCoroutineContextProvider(
        impl: CoroutineContextProviderPlatform
    ): CoroutineContextProvider
}
