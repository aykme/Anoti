package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.edgetoedge

import android.annotation.SuppressLint
import android.os.Build
import android.os.Build.VERSION_CODES.Q

/**
 * enableEdgeToEdge() doesn't work correctly with BottomNavigationView
 * and window.navigationBarColor() or android:statusBarColor (xml)
 * on 28 api level or lower.
 * There is a bug with navigation bar elements color on light theme.
 */
@SuppressLint("AnnotateVersionCheck")
fun isEdgeToEdgeEnabled(): Boolean {
    return Build.VERSION.SDK_INT >= Q
}
