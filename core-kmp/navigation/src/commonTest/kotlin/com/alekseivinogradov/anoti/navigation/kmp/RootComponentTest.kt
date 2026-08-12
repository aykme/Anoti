package com.alekseivinogradov.anoti.navigation.kmp

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.resume
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

private class TestChild(val config: RootConfig)

class RootComponentTest {

    private fun createRoot(
        disposed: MutableList<RootConfig> = mutableListOf(),
        initialConfiguration: RootConfig = RootConfig.AnimeList
    ): RootComponent<TestChild> {
        val lifecycle = LifecycleRegistry()
        val root = RootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            initialConfiguration = initialConfiguration,
            childFactory = { config, childContext ->
                childContext.lifecycle.doOnDestroy { disposed += config }
                TestChild(config = config)
            }
        )
        lifecycle.resume()
        return root
    }

    @Test
    fun startsOnTheInitialConfiguration() {
        val root = createRoot(initialConfiguration = RootConfig.AnimeFavorites)

        assertEquals<RootConfig>(RootConfig.AnimeFavorites, root.childStack.value.active.configuration)
        assertEquals<RootConfig>(RootConfig.AnimeFavorites, root.childStack.value.active.instance.config)
    }

    @Test
    fun navigateToReplacesTheWholeStackInsteadOfPushingOntoIt() {
        val root = createRoot()

        root.navigateTo(RootConfig.AnimeFavorites)
        root.navigateTo(RootConfig.AnimeList)
        root.navigateTo(RootConfig.AnimeFavorites)

        assertEquals<Int>(1, root.childStack.value.items.size)
        assertEquals<RootConfig>(RootConfig.AnimeFavorites, root.childStack.value.active.configuration)
    }

    @Test
    fun navigateToDisposesThePreviousChild() {
        val disposed = mutableListOf<RootConfig>()
        val root = createRoot(disposed = disposed)

        root.navigateTo(RootConfig.AnimeFavorites)

        assertEquals<List<RootConfig>>(listOf(RootConfig.AnimeList), disposed)
    }

    @Test
    fun navigateToDoesNotDisposeTheNewChild() {
        val disposed = mutableListOf<RootConfig>()
        val root = createRoot(disposed = disposed)

        root.navigateTo(RootConfig.AnimeFavorites)

        val isDisposed = RootConfig.AnimeFavorites in disposed
        assertFalse(isDisposed)
    }
}
