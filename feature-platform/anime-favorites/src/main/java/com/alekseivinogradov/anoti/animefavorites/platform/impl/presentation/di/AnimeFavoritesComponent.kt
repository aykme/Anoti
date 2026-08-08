package com.alekseivinogradov.anoti.animefavorites.platform.impl.presentation.di

import com.alekseivinogradov.anoti.animefavorites.platform.impl.presentation.AnimeFavoritesFragment
import com.alekseivinogradov.anoti.di.platform.api.presentation.main.MainComponent
import com.alekseivinogradov.anoti.di.platform.api.presentation.scope.FeatureScope
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
