package com.alekseivinogradov.anoti.di.kmp.scope

/** Marks bindings that live for the app's whole lifetime, merged into the single app-wide graph. */
object AppScope

/** Marks bindings that live for one Activity instance, merged into that Activity's child graph. */
object ActivityScope

/** Marks bindings that live for one screen (Fragment), merged into that screen's child graph. */
object FeatureScope
