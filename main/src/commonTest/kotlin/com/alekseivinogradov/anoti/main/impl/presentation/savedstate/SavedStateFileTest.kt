package com.alekseivinogradov.anoti.main.impl.presentation.savedstate

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SavedStateFileTest {

    private val fileSystem = FakeFileSystem()

    private val directory = "/app/saved_state".toPath()

    private val path = directory / "root_saved_state.json"

    private val file = SavedStateFile(fileSystem = fileSystem, path = path)

    @AfterTest
    fun checkNoOpenFiles() = fileSystem.checkNoOpenFiles()

    @Test
    fun aWrittenTextIsTakenBackAndTakingItDeletesTheFile() {
        //Given
        file.write("saved")

        //When
        val taken = file.take()

        //Then
        assertEquals("saved", taken)
        assertFalse(fileSystem.exists(path))
    }

    @Test
    fun takingAMissingFileGivesNothing() {
        //Given
        val exists = fileSystem.exists(path)

        //When
        val taken = file.take()

        //Then
        assertFalse(exists)
        assertNull(taken)
    }

    @Test
    fun aFileThatCannotBeReadIsDeletedAndGivesNothing() {
        //Given
        fileSystem.createDirectories(path)

        //When
        val taken = file.take()

        //Then
        assertNull(taken)
        assertFalse(fileSystem.exists(path))
    }

    @Test
    fun aWriteReplacesTheOldContentAndLeavesNoTemporaryFile() {
        //Given
        file.write("older")

        //When
        file.write("newer")

        //Then
        assertEquals(listOf(path), fileSystem.list(directory))
        assertEquals("newer", file.take())
    }

    @Test
    fun aWriteCreatesTheMissingDirectory() {
        //Given
        val exists = fileSystem.exists(directory)

        //When
        file.write("saved")

        //Then
        assertFalse(exists)
        assertEquals("saved", file.take())
    }

    @Test
    fun aWriteThatFailsLeavesNoFileAtAll() {
        //Given
        file.write("older")
        fileSystem.createDirectories(directory / "root_saved_state.json.tmp")
        fileSystem.write(directory / "root_saved_state.json.tmp" / "blocker") { writeUtf8("x") }

        //When
        file.write("newer")

        //Then
        assertNull(file.take())
    }

    @Test
    fun deletingRemovesTheFile() {
        //Given
        file.write("saved")

        //When
        file.delete()

        //Then
        assertFalse(fileSystem.exists(path))
    }
}
