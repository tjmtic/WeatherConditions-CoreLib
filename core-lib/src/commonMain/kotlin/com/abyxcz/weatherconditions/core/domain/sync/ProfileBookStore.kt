package com.abyxcz.weatherconditions.core.domain.sync

import com.abyxcz.weatherconditions.core.domain.model.ProfileBook

/** Reads and writes the shared [ProfileBook] sync file (the Tier 1 export format). */
interface ProfileBookStore {
    suspend fun read(): ProfileBook
    suspend fun write(book: ProfileBook)
}

/**
 * The local profile store (the app's database) viewed as a [ProfileBook] so it can be
 * reconciled with the file. [readBook] snapshots current profiles with their edit timestamps;
 * [applyBook] writes a reconciled book back (upsert live profiles, delete tombstoned ones).
 */
interface LocalProfileSource {
    suspend fun readBook(): ProfileBook
    suspend fun applyBook(book: ProfileBook)
}
