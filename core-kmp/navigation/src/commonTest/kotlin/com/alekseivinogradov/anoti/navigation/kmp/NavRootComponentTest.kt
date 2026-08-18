package com.alekseivinogradov.anoti.navigation.kmp

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.resume
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

private class TestChild(val config: NavRootConfig)

class NavRootComponentTest {

    private fun createRoot(
        disposed: MutableList<NavRootConfig> = mutableListOf(),
        initialConfiguration: NavRootConfig = NavRootConfig.AnimeList
    ): NavRootComponent<TestChild> {
        val lifecycle = LifecycleRegistry()
        val root = NavRootComponent(
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
        val root = createRoot(initialConfiguration = NavRootConfig.AnimeFavorites)

        assertEquals(NavRootConfig.AnimeFavorites, root.childStack.value.active.configuration)
        assertEquals(NavRootConfig.AnimeFavorites, root.childStack.value.active.instance.config)
    }

    @Test
    fun navigateToReplacesTheWholeStackInsteadOfPushingOntoIt() {
        val root = createRoot()

        root.navigateTo(NavRootConfig.AnimeFavorites)
        root.navigateTo(NavRootConfig.AnimeList)
        root.navigateTo(NavRootConfig.AnimeFavorites)

        assertEquals(1, root.childStack.value.items.size)
        assertEquals(NavRootConfig.AnimeFavorites, root.childStack.value.active.configuration)
    }

    @Test
    fun navigateToDisposesThePreviousChild() {
        val disposed = mutableListOf<NavRootConfig>()
        val root = createRoot(disposed = disposed)

        root.navigateTo(NavRootConfig.AnimeFavorites)

        assertEquals<List<*>>(listOf(NavRootConfig.AnimeList), disposed)
    }

    @Test
    fun navigateToDoesNotDisposeTheNewChild() {
        val disposed = mutableListOf<NavRootConfig>()
        val root = createRoot(disposed = disposed)

        root.navigateTo(NavRootConfig.AnimeFavorites)

        val isDisposed = NavRootConfig.AnimeFavorites in disposed
        assertFalse(isDisposed)
    }
}
