package com.alekseivinogradov.anime_list.impl.presentation.di

import com.alekseivinogradov.anime_list.impl.presentation.AnimeListFragment
import com.alekseivinogradov.di.api.presentation.main.MainComponent
import com.alekseivinogradov.di.api.presentation.scope.FeatureScope
import dagger.Component

@Component(
    dependencies = [MainComponent::class],
    modules = [AnimeListModule::class]
)
@FeatureScope
interface AnimeListComponent {
    @Component.Factory
    interface Factory {
        fun create(mainComponent: MainComponent): AnimeListComponent
    }

    fun inject(fragment: AnimeListFragment)
}
