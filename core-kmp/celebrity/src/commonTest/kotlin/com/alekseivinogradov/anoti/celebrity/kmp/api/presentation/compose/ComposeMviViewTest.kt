package com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose

import com.arkivanov.mvikotlin.core.rx.observer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class TestComposeMviView : ComposeMviView<String, Int>()

class ComposeMviViewTest {

    @Test
    fun modelStartsAsNull() {
        val view = TestComposeMviView()

        assertNull(view.model.value)
    }

    @Test
    fun renderUpdatesModel() {
        val view = TestComposeMviView()

        view.render("first")
        assertEquals("first", view.model.value)

        view.render("second")
        assertEquals("second", view.model.value)
    }

    @Test
    fun dispatchEmitsToEventsInOrder() {
        val view = TestComposeMviView()
        val emitted = mutableListOf<Int>()
        view.events(observer(onNext = { emitted.add(it) }))

        view.dispatch(1)
        view.dispatch(2)
        view.dispatch(3)

        assertEquals(listOf(1, 2, 3), emitted)
    }
}
