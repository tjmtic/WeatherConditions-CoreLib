package com.abyxcz.weatherconditions.core.domain.sync

import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.ProfileBook
import com.abyxcz.weatherconditions.core.domain.model.StoredProfile
import com.abyxcz.weatherconditions.core.domain.usecase.ProfileSync
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import okio.FileSystem

private class FakeLocal(initial: ProfileBook = ProfileBook()) : LocalProfileSource {
    var book: ProfileBook = initial
    override suspend fun readBook(): ProfileBook = book
    override suspend fun applyBook(book: ProfileBook) {
        this.book = book
    }
}

class ProfileSyncManagerTest : FunSpec({

    fun tempStore(): OkioProfileBookStore {
        val path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "profilebook-${System.nanoTime()}.json"
        return OkioProfileBookStore(FileSystem.SYSTEM, path)
    }

    fun stored(name: String, maxTemp: Int = 80, at: Long, deleted: Boolean = false) =
        StoredProfile(PlayabilityProfile(name = name, maxTemp = maxTemp), updatedAt = at, deleted = deleted)

    test("sync merges local and file both ways") {
        val local = FakeLocal(ProfileBook(profiles = listOf(stored("golf", 70, at = 200))))
        val file = tempStore()
        file.write(ProfileBook(profiles = listOf(stored("dog", at = 100))))

        val merged = ProfileSyncManager(local, file).sync()

        ProfileSync.activeProfiles(merged).map { it.name } shouldContainExactlyInAnyOrder listOf("golf", "dog")
        ProfileSync.activeProfiles(local.book).size shouldBe 2
        ProfileSync.activeProfiles(file.read()).size shouldBe 2
    }

    test("newer agent edit in the file wins on import") {
        val local = FakeLocal(ProfileBook(profiles = listOf(stored("golf", 80, at = 100))))
        val file = tempStore()
        file.write(ProfileBook(profiles = listOf(stored("golf", 65, at = 300))))

        ProfileSyncManager(local, file).import()

        ProfileSync.activeProfiles(local.book).single { it.name == "golf" }.maxTemp shouldBe 65
    }

    test("export writes local profiles into the file") {
        val local = FakeLocal(ProfileBook(profiles = listOf(stored("tennis", at = 500))))
        val file = tempStore()

        ProfileSyncManager(local, file).export()

        ProfileSync.activeProfiles(file.read()).any { it.name == "tennis" } shouldBe true
    }

    test("a tombstone in the file deletes the local profile on sync") {
        val local = FakeLocal(ProfileBook(profiles = listOf(stored("old", at = 100))))
        val file = tempStore()
        file.write(ProfileBook(profiles = listOf(stored("old", at = 300, deleted = true))))

        ProfileSyncManager(local, file).sync()

        ProfileSync.activeProfiles(local.book) shouldBe emptyList()
    }
})
