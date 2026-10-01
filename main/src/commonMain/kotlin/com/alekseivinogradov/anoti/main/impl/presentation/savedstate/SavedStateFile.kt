package com.alekseivinogradov.anoti.main.impl.presentation.savedstate

import okio.FileSystem
import okio.IOException
import okio.Path

private const val TAG = "SavedStateFile"

/**
 * The file a screen host keeps a root's saved state in. Nothing here throws: a file that cannot
 * be written or read counts as no file.
 *
 * @param fileSystem the file system the file lives on.
 * @param path where the file lives. Its directory is created when missing.
 */
internal class SavedStateFile(
    private val fileSystem: FileSystem,
    private val path: Path
) {

    private val directory: Path = checkNotNull(path.parent) { "$path has no directory" }

    // Written first and moved over the file, so a write cut short leaves the old file whole.
    private val temporaryPath: Path = directory / "${path.name}.tmp"

    /**
     * Replaces the file's content with [text], and tells whether it did. A write that fails leaves
     * no file at all.
     */
    fun write(text: String): Boolean =
        try {
            fileSystem.createDirectories(directory)
            fileSystem.write(temporaryPath) { writeUtf8(text) }
            fileSystem.atomicMove(source = temporaryPath, target = path)
            true
        } catch (exception: IOException) {
            println("$TAG: the state was not written: $exception")
            deleteQuietly(temporaryPath)
            delete()
            false
        }

    /**
     * Reads the file and deletes it, or gives `null` when it is missing or cannot be read. A file
     * that cannot be read stays, since the read may succeed later. A text whose file cannot be
     * deleted is not given either: a state that crashes the app on restore would otherwise crash
     * every launch.
     */
    fun take(): String? {
        val text = try {
            if (fileSystem.exists(path)) fileSystem.read(path) { readUtf8() } else null
        } catch (exception: IOException) {
            println("$TAG: the state was not read: $exception")
            null
        }
        return text?.takeIf { delete() }
    }

    /** Deletes the file, if there is one, and tells whether it is gone. */
    fun delete(): Boolean = deleteQuietly(path)

    private fun deleteQuietly(target: Path): Boolean =
        try {
            fileSystem.deleteRecursively(target)
            true
        } catch (exception: IOException) {
            println("$TAG: $target was not deleted: $exception")
            false
        }
}
