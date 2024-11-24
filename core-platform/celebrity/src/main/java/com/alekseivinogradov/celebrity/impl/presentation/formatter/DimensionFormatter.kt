package com.alekseivinogradov.celebrity.impl.presentation.formatter

import android.content.res.Resources

fun Float.dp(): Float {
    return this / Resources.getSystem().displayMetrics.density
}

fun Float.px(): Float {
    return this * Resources.getSystem().displayMetrics.density
}
