package com.abyxcz.weatherconditions.core.ports.outbound

/**
 * Port for cloud backup and synchronization of user data.
 */
interface BackupService {
    /**
     * Upload current local data to the cloud.
     */
    suspend fun uploadBackup(jsonData: String): Result<Unit>

    /**
     * Download data from the cloud.
     */
    suspend fun downloadBackup(): Result<String?>

    /**
     * Check if a backup exists.
     */
    suspend fun hasBackup(): Boolean
}
