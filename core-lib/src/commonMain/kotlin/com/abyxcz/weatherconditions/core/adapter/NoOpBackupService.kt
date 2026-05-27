package com.abyxcz.weatherconditions.core.adapter

import com.abyxcz.weatherconditions.core.ports.outbound.BackupService

/**
 * Default No-Op implementation of BackupService.
 */
class NoOpBackupService : BackupService {
    override suspend fun uploadBackup(jsonData: String): Result<Unit> = Result.success(Unit)
    override suspend fun downloadBackup(): Result<String?> = Result.success(null)
    override suspend fun hasBackup(): Boolean = false
}
