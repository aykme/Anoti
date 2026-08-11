package com.alekseivinogradov.anoti.di.kmp

/**
 * Platform application-context handle, abstracted so DI wiring can live in commonMain.
 * On Android this is `android.content.Context` itself (zero-cost typealias); on iOS it
 * currently carries no state — add fields here if an iOS binding needs one.
 *
 * Declared `abstract`, not the default `final`: the Android `actual` is a typealias to
 * `android.content.Context`, which is itself an abstract class, and an `actual typealias`
 * must match the modality of the type it aliases.
 */
expect abstract class PlatformContext
