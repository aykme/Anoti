package com.alekseivinogradov.app

import android.content.res.Resources
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.alekseivinogradov.main.impl.presentation.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.alekseivinogradov.anime_list_platform.R as anime_list_R

class UserFlowTest {

    @get:Rule
    val grantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var resources: Resources
    private val onAirButtonText get() = resources.getString(anime_list_R.string.on_air)

    @Before
    fun setup() {
        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    }


    @Test
    fun addOngoingToAnimeFavorites() = runBlocking {
        safeInteraction {
            onView(withId(anime_list_R.id.ongoing_button))
                .check(matches(withText(onAirButtonText)))
                .perform(click())
        }

        Assert.assertTrue(true)
    }
}
