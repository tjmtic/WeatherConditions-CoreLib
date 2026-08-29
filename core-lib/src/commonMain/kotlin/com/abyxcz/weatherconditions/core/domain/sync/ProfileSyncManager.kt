package com.abyxcz.weatherconditions.core.domain.sync

import com.abyxcz.weatherconditions.core.domain.model.ProfileBook
import com.abyxcz.weatherconditions.core.domain.usecase.ProfileSync

/**
 * Reconciles the app's local profiles ([LocalProfileSource]) with the shared sync file
 * ([ProfileBookStore]) using the same [ProfileSync] merge as the MCP server and desktop app.
 *
 * The transport that moves the file between phone and desktop is out of scope here — on one
 * machine it's a shared path; for mobile it will be the Bluetooth MCP library. This class only
 * cares that both sides agree on the [ProfileBook] format and last-writer-wins reconciliation.
 */
class ProfileSyncManager(
    private val local: LocalProfileSource,
    private val file: ProfileBookStore,
) {
    /** Full two-way reconcile: merge local + file, then write the result to both. */
    suspend fun sync(): ProfileBook {
        val merged = ProfileSync.merge(local.readBook(), file.read())
        file.write(merged)
        local.applyBook(merged)
        return merged
    }

    /** Push local edits into the file, merging so concurrent agent edits aren't clobbered. */
    suspend fun export(): ProfileBook {
        val merged = ProfileSync.merge(file.read(), local.readBook())
        file.write(merged)
        return merged
    }

    /** Pull file edits (e.g. from the agent) into the local store. */
    suspend fun import(): ProfileBook {
        val merged = ProfileSync.merge(local.readBook(), file.read())
        local.applyBook(merged)
        return merged
    }
}
