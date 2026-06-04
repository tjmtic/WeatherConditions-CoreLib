package com.abyxcz.weatherconditions.core.domain.sync

/**
 * Shared constants for the Bluetooth profile-sync flow, so the mobile app (central) and the
 * desktop bridge (peripheral) agree on the GATT service and the generic-command control plane.
 * Single source of truth to prevent the two sides from drifting.
 */
object ProfileSyncProtocol {
    /** GATT service the bridge advertises and the phone scans for. */
    const val SERVICE_UUID = "8f1c0001-b8a7-4e2a-9c3d-1f2e3a4b5c6d"

    /** Characteristic used for the sync exchange. */
    const val CHARACTERISTIC_UUID = "8f1c0002-b8a7-4e2a-9c3d-1f2e3a4b5c6d"

    /** Generic command: a device announces it is starting a profile sync. */
    const val CMD_PROFILE_SYNC = 1

    /** Generic command: the merge completed. */
    const val CMD_SYNC_DONE = 2
}
