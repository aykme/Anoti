package com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose

import com.arkivanov.mvikotlin.core.rx.observer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class TestComposeMviView(
    initialModel: String? = null
) : ComposeMviView<String, Int>(initialModel)

class ComposeMviViewTest {

    @Test
    fun modelStartsAsNull() {
        //Given
        val view = TestComposeMviView()

        //When
        val model = view.model.value

        //Then
        assertNull(model)
    }

    @Test
    fun modelStartsAtTheInitialModelWhenOneIsGiven() {
        //Given
        val view = TestComposeMviView(initialModel = "seeded")

        //When
        val model = view.model.value

        //Then
        assertEquals("seeded", model)
    }

    @Test
    fun renderUpdatesModel() {
        //Given
        val view = TestComposeMviView()

        //When
        view.render("first")
        val afterFirst = view.model.value
        view.render("second")

        //Then
        assertEquals("first", afterFirst)
        assertEquals("second", view.model.value)
    }

    @Test
    fun dispatchEmitsToEventsInOrder() {
        //Given
        val view = TestComposeMviView()
        val emitted = mutableListOf<Int>()
        view.events(observer(onNext = { emitted.add(it) }))

        //When
        view.dispatch(1)
        view.dispatch(2)
        view.dispatch(3)

        //Then
        assertEquals(listOf(1, 2, 3), emitted)
    }
}
