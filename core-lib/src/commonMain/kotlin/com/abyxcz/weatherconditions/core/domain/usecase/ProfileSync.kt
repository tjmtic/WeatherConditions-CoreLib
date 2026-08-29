package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.ProfileBook
import com.abyxcz.weatherconditions.core.domain.model.StoredProfile

/**
 * Pure reconciliation logic for the shared [ProfileBook] sync file. Identical on both
 * sides (app and MCP server) so bidirectional sync converges deterministically.
 */
object ProfileSync {
    private fun key(profile: PlayabilityProfile): String = profile.name.trim().lowercase()

    /**
     * Last-writer-wins merge keyed by profile name (case-insensitive). For each name the
     * entry with the greater [StoredProfile.updatedAt] wins; on a tie [b] wins. Tombstones
     * participate, so a newer deletion hides an older edit (and vice-versa).
     */
    fun merge(a: ProfileBook, b: ProfileBook): ProfileBook {
        val winners = LinkedHashMap<String, StoredProfile>()
        (a.profiles + b.profiles).forEach { entry ->
            val k = key(entry.profile)
            val current = winners[k]
            if (current == null || entry.updatedAt >= current.updatedAt) {
                winners[k] = entry
            }
        }
        return ProfileBook(
            version = maxOf(a.version, b.version),
            profiles = winners.values.toList(),
        )
    }

    /** Upsert a single profile into a book at [now], replacing any same-named entry. */
    fun put(book: ProfileBook, profile: PlayabilityProfile, now: Long): ProfileBook {
        val others = book.profiles.filterNot { key(it.profile) == key(profile) }
        return book.copy(profiles = others + StoredProfile(profile, now, deleted = false))
    }

    /** Write a deletion tombstone for [name] at [now]; returns the book unchanged if absent. */
    fun remove(book: ProfileBook, name: String, now: Long): ProfileBook {
        val match = book.profiles.firstOrNull { it.profile.name.trim().lowercase() == name.trim().lowercase() }
            ?: return book
        val others = book.profiles.filterNot { it.profile.name.trim().lowercase() == name.trim().lowercase() }
        return book.copy(profiles = others + match.copy(updatedAt = now, deleted = true))
    }

    /** The live (non-deleted) profiles in a book. */
    fun activeProfiles(book: ProfileBook): List<PlayabilityProfile> =
        book.profiles.filter { !it.deleted }.map { it.profile }
}
