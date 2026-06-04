package com.abyxcz.weatherconditions.core.domain.sync

import com.abyxcz.weatherconditions.core.domain.model.ProfileBook
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

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
        path.parent?.let { fileSystem.createDirectories(it) }
        fileSystem.write(path) { writeUtf8(json.encodeToString(ProfileBook.serializer(), book)) }
    }
}
