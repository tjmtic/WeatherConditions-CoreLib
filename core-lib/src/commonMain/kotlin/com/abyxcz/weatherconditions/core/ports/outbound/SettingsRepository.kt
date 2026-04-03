package com.abyxcz.weatherconditions.core.ports.outbound

import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.PlayabilitySettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<PlayabilitySettings>
    suspend fun updateSettings(settings: PlayabilitySettings)
    fun getAllProfiles(): Flow<List<PlayabilityProfile>>
    fun getProfileById(id: Long): Flow<PlayabilityProfile?>
    suspend fun upsertProfile(profile: PlayabilityProfile)
    suspend fun deleteProfile(id: Long)
}
