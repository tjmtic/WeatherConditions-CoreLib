package com.abyxcz.weatherconditions.core.domain.sync

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ProfileSyncProtocolTest : FunSpec({

    test("derivePsk returns a 32-byte key (the size the BLE library expects)") {
        ProfileSyncProtocol.derivePsk("golf-pairing-2026").size shouldBe 32
    }

    test("derivePsk is deterministic for the same code") {
        ProfileSyncProtocol.derivePsk("hunter2").toList() shouldBe
            ProfileSyncProtocol.derivePsk("hunter2").toList()
    }

    test("derivePsk differs for different codes") {
        ProfileSyncProtocol.derivePsk("codeA").toList() shouldNotBe
            ProfileSyncProtocol.derivePsk("codeB").toList()
    }
})
