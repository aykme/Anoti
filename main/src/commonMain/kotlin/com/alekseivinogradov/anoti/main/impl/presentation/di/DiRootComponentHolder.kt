package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionSession

/**
 * Exposes [DiRootComponent] creation, and what every root of the process shares, to callers that
 * only hold an `Application` reference.
 */
interface DiRootComponentHolder {

    /** The notification permission flow of this process. The same instance on every read. */
    val notificationPermissionSession: NotificationPermissionSession

    /** Builds a new [DiRootComponent]. Every call returns its own instance. */
    fun createDiRootComponent(): DiRootComponent
}
