package com.alekseivinogradov.anoti.animebase.kmp.api.domain

const val FIRST_PAGE = 1

/**
 * Anime in one answer: a page of a list, or a batch of ids. It is the server's cap; asking for
 * more silently gets this many.
 */
const val ITEMS_PER_PAGE = 50
const val PAGING_PREFETCH_DISTANCE = 10
