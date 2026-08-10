package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

import com.alekseivinogradov.anoti.animelist.android.impl.presentation.AnimeListFragment
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.scope.FeatureScope
import com.alekseivinogradov.anoti.di.android.api.presentation.main.MainComponent
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
