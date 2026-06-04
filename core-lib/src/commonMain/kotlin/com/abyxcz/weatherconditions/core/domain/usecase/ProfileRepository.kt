package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile

/**
 * The shared contract for reading and writing user scoring profiles, so the app
 * (SQLDelight-backed, the editor) and the MCP server (file-backed, a client) code against
 * one abstraction instead of two divergent stores.
 *
 * Implementations differ in their backing store but agree on the [ProfileBook] wire format
 * and [ProfileSync] reconciliation, which is what lets a single source of truth flow
 * between them.
 */
interface ProfileRepository {
    /** All live (non-deleted) user profiles. */
    suspend fun list(): List<PlayabilityProfile>

    /** A single profile by name (case-insensitive), or null if absent/deleted. */
    suspend fun get(name: String): PlayabilityProfile?

    /** Create or replace a profile, returning the stored value. */
    suspend fun upsert(profile: PlayabilityProfile): PlayabilityProfile

    /** Delete a profile by name; returns true if one was removed. */
    suspend fun delete(name: String): Boolean
}
