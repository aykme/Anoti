package com.alekseivinogradov.anime_favorites.impl.presentation.di

import com.alekseivinogradov.anime_favorites.impl.presentation.AnimeFavoritesFragment
import com.alekseivinogradov.di.api.presentation.main.MainComponent
import com.alekseivinogradov.di.api.presentation.scope.FeatureScope
import dagger.Component

@Component(
    dependencies = [MainComponent::class],
    modules = [AnimeFavoritesModule::class]
)
@FeatureScope
interface AnimeFavoritesComponent {

    @Component.Factory
    interface Factory {
        fun create(mainComponent: MainComponent): AnimeFavoritesComponent
    }

    fun inject(fragment: AnimeFavoritesFragment)
}
