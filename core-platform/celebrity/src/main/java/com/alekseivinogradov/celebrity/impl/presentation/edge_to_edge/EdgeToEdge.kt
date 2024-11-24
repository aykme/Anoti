package com.alekseivinogradov.celebrity.impl.presentation.edge_to_edge

import android.os.Build
import android.os.Build.VERSION_CODES.Q
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * enableEdgeToEdge() doesn't work correctly with BottomNavigationView
 * and window.navigationBarColor() or android:statusBarColor (xml)
 * on 28 api level or lower.
 * There is a bug with navigation bar elements color on light theme.
 */
@ChecksSdkIntAtLeast(api = Q)
fun isEdgeToEdgeEnabled(): Boolean {
    return Build.VERSION.SDK_INT >= Q
}
