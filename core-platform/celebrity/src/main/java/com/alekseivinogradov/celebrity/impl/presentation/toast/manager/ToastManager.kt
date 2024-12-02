package com.alekseivinogradov.celebrity.impl.presentation.toast.manager

import android.content.Context
import android.widget.Toast
import com.alekseivinogradov.date_platform.R

object ToastManager {

    fun makeLongToastWithResId(appContext: Context, resId: Int) {
        Toast.makeText(
            /* context = */ appContext.applicationContext,
            /* resId = */ resId,
            /* duration = */ Toast.LENGTH_LONG
        ).show()
    }

    fun makeConnectionErrorToast(appContext: Context) {
        makeLongToastWithResId(
            appContext = appContext,
            resId = R.string.connection_error
        )
    }

    fun makeUnknownErrorToast(appContext: Context) {
        makeLongToastWithResId(
            appContext = appContext,
            resId = R.string.unknown_error
        )
    }
}
