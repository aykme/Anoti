package com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store

import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogStore
import kotlin.test.Test
import kotlin.test.assertEquals

class InfoDialogReducerImplTest {

    private val reducer = InfoDialogReducerImpl()

    private fun reduce(
        state: InfoDialogStore.State,
        msg: InfoDialogStore.Message
    ): InfoDialogStore.State {
        return with(reducer) { state.reduce(msg) }
    }

    @Test
    fun changeVisibilityToShownShowsAHiddenDialog() {
        //Given
        val state = InfoDialogStore.State(isShown = false)

        //When
        val reduced = reduce(state, InfoDialogStore.Message.ChangeVisibility(isShown = true))

        //Then
        assertEquals(InfoDialogStore.State(isShown = true), reduced)
    }

    @Test
    fun changeVisibilityToHiddenHidesAShownDialog() {
        //Given
        val state = InfoDialogStore.State(isShown = true)

        //When
        val reduced = reduce(state, InfoDialogStore.Message.ChangeVisibility(isShown = false))

        //Then
        assertEquals(InfoDialogStore.State(isShown = false), reduced)
    }
}
