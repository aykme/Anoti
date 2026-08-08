package com.alekseivinogradov.anoti.animelist.platform.impl.presentation.di

import com.alekseivinogradov.anoti.animelist.platform.impl.presentation.AnimeListFragment
import com.alekseivinogradov.anoti.di.platform.api.presentation.main.MainComponent
import com.alekseivinogradov.anoti.di.platform.api.presentation.scope.FeatureScope
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
