package com.alekseivinogradov.celebrity.impl.presentation.toast

import android.content.Context
import android.widget.Toast
import com.alekseivinogradov.date_platform.R

object AnotiToast {

    fun makeLongToastWithResId(context: Context, resId: Int) {
        Toast.makeText(
            /* context = */ context,
            /* resId = */ resId,
            /* duration = */ Toast.LENGTH_LONG
        ).show()
    }

    fun makeConnectionErrorToast(context: Context) {
        makeLongToastWithResId(
            context = context,
            resId = R.string.connection_error
        )
    }

    fun makeUnknownErrorToast(context: Context) {
        makeLongToastWithResId(
            context = context,
            resId = R.string.unknown_error
        )
    }
}
