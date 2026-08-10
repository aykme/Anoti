package com.alekseivinogradov.anoti.testutils.android.action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction

/** An [ViewAction] that clicks the child view with id [childId] inside the matched view. */
fun clickOnChildView(childId: Int) = object : ViewAction {
    override fun getConstraints() = null

    override fun getDescription() = "Click on a child view with id: $childId"

    override fun perform(uiController: UiController, view: View) {
        val childView: View = view.findViewById(childId)
        childView.performClick()
    }
}
