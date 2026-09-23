package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source.fake.AnimeBackgroundUpdateSourceFake
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.fake.AnimeDatabaseUsecasesFake
import com.alekseivinogradov.anoti.animenotification.kmp.impl.domain.manager.fake.AnimeNotificationManagerFake
import com.alekseivinogradov.anoti.animenotification.kmp.impl.domain.manager.fake.NewEpisodeNotification
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val POSTER_URL_PREFIX = "https://shikimori.io/system/animes/original/"
private const val AIRED_ON = "2025-10-01"
private const val NEXT_EPISODE_AT = "2026-01-05T18:00:00.000+03:00"
private const val EPISODES_TOTAL = 12
private const val SCORE = 8.0F

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class AnimeUpdateManagerImplTest {

    @Test
    fun theWholeLibraryIsAskedForOnePageOfIdsAtATime() = runTest {
        //Given
        val savedCount = ITEMS_PER_PAGE + 5
        val database = databaseWith((1..savedCount).map(::savedAnime))
        val source = AnimeBackgroundUpdateSourceFake()

        //When
        createManager(database = database, source = source).update()

        //Then
        assertEquals(
            listOf(
                (1..ITEMS_PER_PAGE).joinToString(separator = ","),
                (ITEMS_PER_PAGE + 1..savedCount).joinToString(separator = ",")
            ),
            source.requestedIds
        )
    }

    @Test
    fun anEmptyLibraryIsReportedAsSuccessWithoutAskingTheServer() = runTest {
        //Given
        val database = databaseWith(listOf())
        val source = AnimeBackgroundUpdateSourceFake()

        //When
        val result = createManager(database = database, source = source).update()

        //Then
        assertEquals(WorkResult.Success, result)
        assertEquals(listOf(), source.requestedIds)
    }

    @Test
    fun aPassWhereEveryFetchSucceedsReportsSuccess() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1)))
        val source = AnimeBackgroundUpdateSourceFake { succeedWith(remoteAnime(1)) }

        //When
        val result = createManager(database = database, source = source).update()

        //Then
        assertEquals(WorkResult.Success, result)
    }

    @Test
    fun aFailedPageIsReportedAsErrorWhileTheOtherPagesAreStillApplied() = runTest {
        //Given
        val savedCount = ITEMS_PER_PAGE + 1
        val database = databaseWith((1..savedCount).map(::savedAnime))
        val lastId = savedCount.toString()
        val source = AnimeBackgroundUpdateSourceFake { ids: String ->
            if (ids == lastId) {
                CallResult.NetworkError(Throwable("no route to host"))
            } else {
                succeedWith(remoteAnime(1, episodesAired = 2))
            }
        }

        //When
        val result = createManager(database = database, source = source).update()

        //Then
        assertEquals(WorkResult.Error, result)
        assertEquals(listOf(1), database.updatedItems.map(AnimeDbDomain::id))
    }

    @Test
    fun anAnimeMissingFromTheResponseIsLeftAlone() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1), savedAnime(2)))
        val source = AnimeBackgroundUpdateSourceFake {
            succeedWith(remoteAnime(2, episodesAired = 5))
        }

        //When
        createManager(database = database, source = source).update()

        //Then
        assertEquals(listOf(2), database.updatedItems.map(AnimeDbDomain::id))
    }

    @Test
    fun aRemoteAnimeNobodySavedIsIgnored() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1)))
        val source = AnimeBackgroundUpdateSourceFake {
            succeedWith(remoteAnime(1), remoteAnime(99, episodesAired = 7))
        }

        //When
        createManager(database = database, source = source).update()

        //Then
        assertEquals(listOf(), database.updatedItems)
    }

    @Test
    fun aNewlyAiredEpisodeIsWrittenAndNotified() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1, episodesAired = 6)))
        val notifications = AnimeNotificationManagerFake()
        val source = AnimeBackgroundUpdateSourceFake {
            succeedWith(remoteAnime(1, episodesAired = 7))
        }

        //When
        createManager(
            database = database,
            source = source,
            notifications = notifications
        ).update()

        //Then
        val written = database.updatedItems.single()
        assertEquals(7, written.episodesAired)
        assertTrue(written.isNewEpisode, "the favorites screen shows the mark, not the count")
        assertEquals(
            listOf(NewEpisodeNotification("Anime 1", 7, POSTER_URL_PREFIX + "1.jpg")),
            notifications.notifications
        )
    }

    @Test
    fun anAnimeThatFinishedAiringIsNotifiedWithItsTotalEpisodeCount() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1, episodesAired = 11)))
        val notifications = AnimeNotificationManagerFake()
        val source = AnimeBackgroundUpdateSourceFake {
            succeedWith(
                remoteAnime(1, episodesAired = 11, releaseStatus = ReleaseStatusDomain.RELEASED)
            )
        }

        //When
        createManager(
            database = database,
            source = source,
            notifications = notifications
        ).update()

        //Then
        // The count did not move, so only the status change makes this a notification. A finished
        // anime is announced by its total rather than by what the server last reported as aired.
        assertEquals(EPISODES_TOTAL, notifications.notifications.single().airedEpisode)
    }

    @Test
    fun aResponseWithNoEpisodeCountNotifiesNobodyAndOverwritesTheSavedCount() = runTest {
        //Given
        // Every field of the response is optional, so a count the server omits arrives as null.
        val database = databaseWith(listOf(savedAnime(1, episodesAired = 5)))
        val notifications = AnimeNotificationManagerFake()
        val source = AnimeBackgroundUpdateSourceFake {
            succeedWith(remoteAnime(1, episodesAired = null))
        }

        //When
        createManager(
            database = database,
            source = source,
            notifications = notifications
        ).update()

        //Then
        // A missing count reads as zero, so nothing looks newly aired and nobody is told. The
        // row is still rewritten, and the count the app knew is gone.
        assertEquals(listOf(), notifications.notifications)
        assertEquals(null, database.updatedItems.single().episodesAired)
    }

    @Test
    fun anAnimeFinishingWithNoTotalIsNotifiedWithWhatLastAired() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1, episodesAired = 9)))
        val notifications = AnimeNotificationManagerFake()
        val source = AnimeBackgroundUpdateSourceFake {
            succeedWith(
                remoteAnime(
                    id = 1,
                    episodesAired = 9,
                    episodesTotal = null,
                    releaseStatus = ReleaseStatusDomain.RELEASED
                )
            )
        }

        //When
        createManager(
            database = database,
            source = source,
            notifications = notifications
        ).update()

        //Then
        // A finished anime is announced by its total, and with no total the last aired episode
        // is the only number left to name.
        assertEquals(9, notifications.notifications.single().airedEpisode)
    }

    @Test
    fun theNewEpisodeMarkStaysOnUntilSomethingElseTakesItOff() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1, isNewEpisode = true, score = 7.0F)))
        val source = AnimeBackgroundUpdateSourceFake { succeedWith(remoteAnime(1)) }

        //When
        createManager(database = database, source = source).update()

        //Then
        assertTrue(database.updatedItems.single().isNewEpisode)
    }

    @Test
    fun anUnchangedAnimeIsNeitherNotifiedNorWritten() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1)))
        val notifications = AnimeNotificationManagerFake()
        val source = AnimeBackgroundUpdateSourceFake { succeedWith(remoteAnime(1)) }

        //When
        createManager(
            database = database,
            source = source,
            notifications = notifications
        ).update()

        //Then
        assertEquals(listOf(), notifications.notifications)
        assertEquals(listOf(), database.updatedItems)
    }

    @Test
    fun theRowIsNotWrittenWhenItsOwnNotificationFails() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1, episodesAired = 6)))
        val notifications = AnimeNotificationManagerFake(
            onNotify = { error("the poster download blew up") }
        )
        val source = AnimeBackgroundUpdateSourceFake {
            succeedWith(remoteAnime(1, episodesAired = 7))
        }

        //When
        val result = createManager(
            database = database,
            source = source,
            notifications = notifications
        ).update()

        //Then
        // Notifying comes first, so a failure there takes the write-down with it.
        assertEquals(WorkResult.Error, result)
        assertEquals(listOf(), database.updatedItems)
    }

    @Test
    fun whatTheUserOwnsOnTheRowSurvivesTheUpdate() = runTest {
        //Given
        val saved = savedAnime(1, episodesAired = 6).copy(
            episodesViewed = 4,
            isExtraInfoEnabled = true
        )
        val database = databaseWith(listOf(saved))
        val source = AnimeBackgroundUpdateSourceFake {
            succeedWith(remoteAnime(1, episodesAired = 7))
        }

        //When
        createManager(database = database, source = source).update()

        //Then
        val written = database.updatedItems.single()
        assertEquals(4, written.episodesViewed)
        assertTrue(written.isExtraInfoEnabled)
        assertEquals(NEXT_EPISODE_AT, written.nextEpisodeAt, "the server never sends this one")
    }

    @Test
    fun twoPassesOverlappingEachNotifyAboutTheSameEpisode() = runTest {
        //Given
        // The hourly pass and the one the favorites button starts are separate work, so the
        // platform is free to run them at the same time over the same library.
        val database = databaseWith(listOf(savedAnime(1, episodesAired = 6)))
        val notifications = AnimeNotificationManagerFake()
        val bothPassesReachedTheServer = CompletableDeferred<Unit>()
        val source = AnimeBackgroundUpdateSourceFake {
            bothPassesReachedTheServer.await()
            succeedWith(remoteAnime(1, episodesAired = 7))
        }
        val manager = createManager(
            database = database,
            source = source,
            notifications = notifications
        )

        //When
        // Started on a dispatcher that runs them right away, so the second pass is inside its
        // fetch before the first one gets its answer. Queued instead, they would simply follow
        // one another and this would not be an overlap at all.
        val eagerly = UnconfinedTestDispatcher(testScheduler)
        val first = launch(eagerly) { manager.update() }
        val second = launch(eagerly) { manager.update() }
        bothPassesReachedTheServer.complete(Unit)
        first.join()
        second.join()

        //Then
        // Both were inside the same fetch, so neither had written the new count when the other
        // read it. One aired episode reaches the user as two notifications.
        assertEquals(2, source.requestedIds.size, "the passes did not overlap")
        assertEquals(2, notifications.notifications.size)
        assertEquals(2, database.updatedItems.size)
    }

    @Test
    fun aDatabaseThatWillNotOpenIsReportedAsErrorRatherThanEscaping() = runTest {
        //Given
        // The fetch cannot throw — it sits behind SafeApi, which turns a throwable into a
        // result. The database read is the step before that, with nothing catching for it.
        val database = AnimeDatabaseUsecasesFake(
            initialItems = listOf(savedAnime(1)),
            onRead = { error("the database would not open") }
        )
        val source = AnimeBackgroundUpdateSourceFake()

        //When
        val result = createManager(database = database, source = source).update()

        //Then
        assertEquals(WorkResult.Error, result)
        assertEquals(listOf(), source.requestedIds)
    }

    @Test
    fun cancellingThePassIsNotReportedAsAnError() = runTest {
        //Given
        val database = databaseWith(listOf(savedAnime(1)))
        val source = AnimeBackgroundUpdateSourceFake { throw CancellationException("stopped") }

        //When / Then
        // A stopped worker must not look like a failed one, or it would count as a real failure.
        assertFailsWith<CancellationException> {
            createManager(database = database, source = source).update()
        }
    }

    private fun TestScope.createManager(
        database: AnimeDatabaseUsecasesFake,
        source: AnimeBackgroundUpdateSourceFake,
        notifications: AnimeNotificationManagerFake = AnimeNotificationManagerFake()
    ) = AnimeUpdateManagerImpl(
        coroutineContextProvider = CoroutineContextProviderFake(
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            workManagerCoroutineContext = UnconfinedTestDispatcher(testScheduler)
        ),
        fetchAllAnimeDatabaseItemsUsecase = database.fetchAllAnimeDatabaseItemsUsecase,
        fetchAnimeListByIdsUsecase = FetchAnimeListByIdsUsecase(source),
        updateAnimeDatabaseItemUsecase = database.usecases.updateAnimeDatabaseItemUsecase,
        notificationManager = notifications
    )

    private fun databaseWith(items: List<AnimeDbDomain>) = AnimeDatabaseUsecasesFake(items)

    private fun succeedWith(vararg items: ListItemDomain) = CallResult.Success(items.toList())

    private fun savedAnime(
        id: AnimeId,
        episodesAired: Int? = 1,
        isNewEpisode: Boolean = false,
        score: Float? = SCORE
    ) = AnimeDbDomain(
        id = id,
        imageUrl = "$POSTER_URL_PREFIX$id.jpg",
        name = "Anime $id",
        episodesAired = episodesAired,
        episodesTotal = EPISODES_TOTAL,
        nextEpisodeAt = NEXT_EPISODE_AT,
        airedOn = AIRED_ON,
        releasedOn = null,
        score = score,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 0,
        isNewEpisode = isNewEpisode,
        isExtraInfoEnabled = false
    )

    private fun remoteAnime(
        id: AnimeId,
        episodesAired: Int? = 1,
        episodesTotal: Int? = EPISODES_TOTAL,
        releaseStatus: ReleaseStatusDomain = ReleaseStatusDomain.ONGOING
    ) = ListItemDomain(
        id = id,
        name = "Anime $id",
        imageUrl = "$POSTER_URL_PREFIX$id.jpg",
        episodesAired = episodesAired,
        episodesTotal = episodesTotal,
        airedOn = AIRED_ON,
        releasedOn = null,
        score = SCORE,
        releaseStatus = releaseStatus
    )
}
