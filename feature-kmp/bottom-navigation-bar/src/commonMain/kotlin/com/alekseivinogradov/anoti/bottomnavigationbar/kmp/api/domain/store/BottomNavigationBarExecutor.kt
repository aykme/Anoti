package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias BottomNavigationBarExecutor = CoroutineExecutor<
    BottomNavigationBarStore.Intent,
    BottomNavigationBarStore.Action,
    BottomNavigationBarStore.State,
    BottomNavigationBarStore.Message,
    BottomNavigationBarStore.Label
    >
