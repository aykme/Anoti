package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimeBackgroundUpdateDatabaseMapperTest {

    @Test
    fun everyReleaseStatusKeepsItsMeaningOnTheWayIntoTheDatabase() {
        //Given
        val allStatuses = ReleaseStatusDomain.entries

        //When
        val mapped = allStatuses.map(::mapReleaseStatusDomainToDb)

        //Then
        // A swapped pair would silently mark a finished anime as still airing, which decides
        // both the new-episode notification and what the favorites row shows.
        assertEquals(
            allStatuses.map(ReleaseStatusDomain::name),
            mapped.map(ReleaseStatusDb::name)
        )
    }
}
