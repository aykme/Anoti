package com.alekseivinogradov.di.api.presentation.main

import android.content.Context
import com.alekseivinogradov.di.api.presentation.AppContext

interface MainComponent {
    @AppContext
    fun provideAppContextFromActivity(): Context
}
