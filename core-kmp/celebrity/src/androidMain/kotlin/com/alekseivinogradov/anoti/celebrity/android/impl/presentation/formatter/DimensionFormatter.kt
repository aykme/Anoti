@file:Suppress("unused")

package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.formatter

import android.content.res.Resources

fun Float.dp(): Float {
    return this / Resources.getSystem().displayMetrics.density
}

fun Float.px(): Float {
    return this * Resources.getSystem().displayMetrics.density
}
