package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent

/** Exposes [DiRootComponent] creation to callers that only hold an `Application` reference. */
interface DiRootComponentHolder {
    fun createDiRootComponent(): DiRootComponent
}
