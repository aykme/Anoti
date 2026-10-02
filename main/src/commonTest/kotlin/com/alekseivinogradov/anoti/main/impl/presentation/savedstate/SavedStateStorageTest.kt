package com.alekseivinogradov.anoti.main.impl.presentation.savedstate

import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SavedStateStorageTest {

    private val fileSystem = FakeFileSystem()

    private val path = "/app/saved_state/root_saved_state.json".toPath()

    private val storage = SavedStateStorage(
        file = SavedStateFile(fileSystem = fileSystem, path = path),
        appVersion = "1.1 (10)",
        windowSessionId = { "window-session" }
    )

    @AfterTest
    fun checkNoOpenFiles() = fileSystem.checkNoOpenFiles()

    @Test
    fun aSavedStateComesBackForTheSameVersionAndSession() {
        //Given
        storage.save(rootNumber = 1) { StateKeeperDispatcher().save() }

        //When
        val taken = storage.take(isDiscarded = false)

        //Then
        assertNotNull(taken)
        assertFalse(fileSystem.exists(path))
    }

    @Test
    fun aDiscardedStateIsNotGivenAndItsFileIsGone() {
        //Given
        storage.save(rootNumber = 1) { StateKeeperDispatcher().save() }

        //When
        val taken = storage.take(isDiscarded = true)

        //Then
        assertNull(taken)
        assertFalse(fileSystem.exists(path))
    }

    @Test
    fun discardingDeletesTheSavedState() {
        //Given
        storage.save(rootNumber = 1) { StateKeeperDispatcher().save() }

        //When
        storage.discard()

        //Then
        assertNull(storage.take(isDiscarded = false))
    }
}
