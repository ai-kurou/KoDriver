package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LowVolumeWarningEnabledPreferencesRepository {
    fun observeLowVolumeWarningEnabled(): Flow<Boolean>

    suspend fun saveLowVolumeWarningEnabled(enabled: Boolean)
}
