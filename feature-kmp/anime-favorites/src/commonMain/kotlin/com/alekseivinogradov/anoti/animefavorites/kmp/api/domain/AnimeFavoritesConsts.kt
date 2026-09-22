package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * How long opening or refreshing the section waits for the database to answer before it settles
 * the loading state against the items it already has. Long enough that a local read always wins
 * the race; short enough that a missing answer cannot leave the screen loading for good.
 */
val LIST_ARRIVAL_TIMEOUT_SECONDS: Duration = 5L.seconds
