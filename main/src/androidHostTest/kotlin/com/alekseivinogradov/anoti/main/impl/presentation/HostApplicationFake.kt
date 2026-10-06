package com.alekseivinogradov.anoti.main.impl.presentation

import android.app.Application
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.di.create
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponentHolder
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionSession

/**
 * Scaffolding, not a subject: the activity reads its root graph off whatever `Application` the
 * process happens to have, so a test that launches the activity has to put one there. This one
 * hands out a graph built from fakes.
 */
internal class HostApplicationFake : Application(), DiRootComponentHolder {

    val dependencies = DiRootDependenciesFake()

    // Replaced by a test that stands for a new process, the way a killed app comes back.
    override var notificationPermissionSession = NotificationPermissionSession()

    override fun createDiRootComponent(): DiRootComponent =
        DiRootComponent::class.create(parent = dependencies)
}
