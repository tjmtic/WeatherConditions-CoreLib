package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.ProfileBook
import com.abyxcz.weatherconditions.core.domain.model.StoredProfile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class ProfileSyncTest : FunSpec({

    fun profile(name: String, maxTemp: Int = 80) = PlayabilityProfile(name = name, maxTemp = maxTemp)
    fun book(vararg entries: StoredProfile) = ProfileBook(profiles = entries.toList())

    test("newer edit wins the merge") {
        val app = book(StoredProfile(profile("golf", maxTemp = 70), updatedAt = 200))
        val mcp = book(StoredProfile(profile("golf", maxTemp = 80), updatedAt = 100))
        val merged = ProfileSync.merge(app, mcp)
        ProfileSync.activeProfiles(merged).single().maxTemp shouldBe 70
    }

    test("newer tombstone hides an older edit (deletes propagate)") {
        val edit = book(StoredProfile(profile("surf"), updatedAt = 100))
        val del = book(StoredProfile(profile("surf"), updatedAt = 300, deleted = true))
        val merged = ProfileSync.merge(edit, del)
        ProfileSync.activeProfiles(merged) shouldBe emptyList()
    }

    test("older tombstone loses to a newer re-create") {
        val del = book(StoredProfile(profile("surf"), updatedAt = 100, deleted = true))
        val recreate = book(StoredProfile(profile("surf", maxTemp = 90), updatedAt = 400))
        val merged = ProfileSync.merge(del, recreate)
        ProfileSync.activeProfiles(merged).single().maxTemp shouldBe 90
    }

    test("disjoint profiles from both sides are unioned") {
        val app = book(StoredProfile(profile("golf"), updatedAt = 100))
        val mcp = book(StoredProfile(profile("dog-walk"), updatedAt = 100))
        val merged = ProfileSync.merge(app, mcp)
        ProfileSync.activeProfiles(merged).map { it.name } shouldContainExactlyInAnyOrder listOf("golf", "dog-walk")
    }

    test("merge keys case-insensitively by name") {
        val a = book(StoredProfile(profile("Golf", maxTemp = 70), updatedAt = 200))
        val b = book(StoredProfile(profile("golf", maxTemp = 80), updatedAt = 100))
        ProfileSync.activeProfiles(ProfileSync.merge(a, b)).size shouldBe 1
    }

    test("put then remove leaves a tombstone, not a live profile") {
        var b = ProfileBook()
        b = ProfileSync.put(b, profile("tennis"), now = 10)
        ProfileSync.activeProfiles(b).single().name shouldBe "tennis"
        b = ProfileSync.remove(b, "tennis", now = 20)
        ProfileSync.activeProfiles(b) shouldBe emptyList()
        b.profiles.single().deleted shouldBe true
    }
})
