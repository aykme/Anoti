package com.alekseivinogradov.anoti.animelist.kmp.api.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

val SEARCH_DEBOUNCE_MILLISECONDS: Duration = 500L.milliseconds

const val SEARCH_TEXT_MAX_LENGTH = 75

/** Most items a section pages in to reach the position saved before a restore. */
const val RESTORED_SECTION_MAX_ITEM_COUNT = 80
