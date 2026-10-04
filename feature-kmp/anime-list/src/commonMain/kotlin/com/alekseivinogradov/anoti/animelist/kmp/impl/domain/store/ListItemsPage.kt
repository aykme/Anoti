package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain

/**
 * This list with [page] appended, leaving out every item whose id is already in it. The
 * backend's ranking can shift between two page requests and repeat an item, and the list keys
 * its rows by id, so an id may appear only once.
 */
internal fun List<ListItemDomain>.plusPage(page: List<ListItemDomain>): List<ListItemDomain> {
    val ids = mapTo(HashSet()) { it.id }
    return this + page.filter { ids.add(it.id) }
}
