package com.alekseivinogradov.bottom_navigation_bar.api.domain.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias BottomNavigationBarExecutor = CoroutineExecutor<
        BottomNavigationBarStore.Intent,
        BottomNavigationBarStore.Action,
        BottomNavigationBarStore.State,
        BottomNavigationBarStore.Message,
        BottomNavigationBarStore.Label
        >
