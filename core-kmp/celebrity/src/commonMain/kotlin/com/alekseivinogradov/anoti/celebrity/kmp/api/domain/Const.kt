package com.alekseivinogradov.anoti.celebrity.kmp.api.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

const val FIRST_PAGE = 1
const val ITEMS_PER_PAGE = 20
const val PAGING_PREFETCH_DISTANCE = 10
val SEARCH_DEBOUNCE_MILLISECONDS: Duration = 500L.milliseconds
