package com.alekseivinogradov.anoti.di.kmp.scope

import me.tatarka.inject.annotations.Scope

/** Marks bindings that live for the app's whole lifetime, held by the app-wide component. */
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope

/** Marks bindings that live for one root UI host, held by that host's child component. */
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class RootScope

/** Marks bindings that live for one screen, held by that screen's child component. */
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class FeatureScope
