package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.main.impl.presentation.navigation.AnimeFavoritesDeepLinkNavigatorImpl
import com.alekseivinogradov.anoti.navigation.platform.api.presentation.AnimeFavoritesDeepLinkNavigator
import dagger.Module
import dagger.Provides

@Module
interface AnimeFavoritesDeepLinkNavigatorModule {
    companion object {
        @Provides
        fun provideAnimeFavoritesDeepLinkNavigator(
            impl: AnimeFavoritesDeepLinkNavigatorImpl
        ): AnimeFavoritesDeepLinkNavigator = impl
    }
}
