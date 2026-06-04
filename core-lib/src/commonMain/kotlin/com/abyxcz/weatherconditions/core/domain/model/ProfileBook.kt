package com.abyxcz.weatherconditions.core.domain.model

import kotlinx.serialization.Serializable

/**
 * One profile entry in a [ProfileBook], tagged for last-writer-wins reconciliation.
 *
 * @property profile the scoring profile.
 * @property updatedAt epoch milliseconds of the last edit; the higher value wins a merge.
 * @property deleted a tombstone — when true the profile was deleted, so the deletion
 *   propagates across systems instead of the entry being resurrected on the next sync.
 */
@Serializable
data class StoredProfile(
    val profile: PlayabilityProfile,
    val updatedAt: Long,
    val deleted: Boolean = false,
)

/**
 * The portable, serializable representation of a user's scoring profiles — the shared
 * "Tier 1" sync file that both the app and the MCP server read and write. Keyed by profile
 * name (case-insensitive) so it is independent of either system's local primary keys.
 *
 * @property syncedAt epoch milliseconds of the last successful transport sync (e.g. the
 *   Bluetooth bridge stamps this when it writes after a device sync), so consumers can
 *   report freshness ("settings last synced N minutes ago"). Null if never synced.
 */
@Serializable
data class ProfileBook(
    val version: Int = 1,
    val profiles: List<StoredProfile> = emptyList(),
    val syncedAt: Long? = null,
)
