package com.alekseivinogradov.celebrity.api.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

const val LIST_FIRST_INDEX = 0
const val FIRST_PAGE = 1
const val ITEMS_PER_PAGE = 20
const val PAGING_PREFETCH_DISTANCE = 10
val SEARCH_DEBOUNCE_MILLISECONDS: Duration = 500L.milliseconds
val PAGING_SUBMIT_LIST_DELAY_MILLISECONDS: Duration = 10L.milliseconds
const val SWIPE_REFRESH_START_OFFSET = 45
const val SWIPE_REFRESH_END_OFFSET = 245
const val REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS = 500L
const val REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS = 200L
