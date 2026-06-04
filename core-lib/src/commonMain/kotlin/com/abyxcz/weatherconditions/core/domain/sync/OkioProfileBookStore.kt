package com.abyxcz.weatherconditions.core.domain.sync

import com.abyxcz.weatherconditions.core.domain.model.ProfileBook
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Multiplatform [ProfileBookStore] backed by an okio file — usable from Android, iOS, watchOS,
 * and the JVM. The platform supplies a [FileSystem] (e.g. `FileSystem.SYSTEM`) and a [Path];
 * the format is the same JSON `ProfileBook` the MCP server and desktop app use.
 */
class OkioProfileBookStore(
    private val fileSystem: FileSystem,
    private val path: Path,
    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : ProfileBookStore {
    override suspend fun read(): ProfileBook {
        if (!fileSystem.exists(path)) return ProfileBook()
        val text = fileSystem.read(path) { readUtf8() }
        return runCatching { json.decodeFromString(ProfileBook.serializer(), text) }
            .getOrDefault(ProfileBook())
    }

    override suspend fun write(book: ProfileBook) {
        val parent = path.parent
        parent?.let { fileSystem.createDirectories(it) }
        // Write to a temp sibling then atomically move, so a reader (e.g. the MCP server)
        // never observes a half-written file.
        val tmp = (parent ?: ".".toPath()) / "${path.name}.tmp"
        fileSystem.write(tmp) { writeUtf8(json.encodeToString(ProfileBook.serializer(), book)) }
        fileSystem.atomicMove(tmp, path)
    }
}
