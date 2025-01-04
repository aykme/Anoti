package com.alekseivinogradov.test_utils.action

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction

fun clickOnChildView(childId: Int) = object : ViewAction {
    override fun getConstraints() = null

    override fun getDescription() = "Click on a child view with id: $childId"

    override fun perform(uiController: UiController, view: View) {
        val childView: View = view.findViewById(childId)
        childView.performClick()
    }
}
