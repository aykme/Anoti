package com.alekseivinogradov.anoti.navigation.kmp

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.resume
import kotlin.test.Test
import kotlin.test.assertEquals

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
        //Given
        val root = createRoot(initialConfiguration = NavRootConfig.AnimeFavorites)

        //When
        val active = root.childStack.value.active

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, active.configuration)
        assertEquals(NavRootConfig.AnimeFavorites, active.instance.config)
    }

    @Test
    fun startsOnTheAnimeListWhenNoConfigurationIsGiven() {
        //Given
        val lifecycle = LifecycleRegistry()

        //When
        val root = NavRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            childFactory = { config, _ -> TestChild(config = config) }
        )
        lifecycle.resume()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.childStack.value.active.configuration)
    }

    @Test
    fun navigateToReplacesTheWholeStackInsteadOfPushingOntoIt() {
        //Given
        val root = createRoot()

        //When
        root.navigateTo(NavRootConfig.AnimeFavorites)
        root.navigateTo(NavRootConfig.AnimeList)
        root.navigateTo(NavRootConfig.AnimeFavorites)

        //Then
        assertEquals(1, root.childStack.value.items.size)
        assertEquals(NavRootConfig.AnimeFavorites, root.childStack.value.active.configuration)
    }

    @Test
    fun navigateToDisposesThePreviousChild() {
        //Given
        val disposed = mutableListOf<NavRootConfig>()
        val root = createRoot(disposed = disposed)

        //When
        root.navigateTo(NavRootConfig.AnimeFavorites)

        //Then
        assertEquals<List<*>>(listOf(NavRootConfig.AnimeList), disposed)
    }
}
