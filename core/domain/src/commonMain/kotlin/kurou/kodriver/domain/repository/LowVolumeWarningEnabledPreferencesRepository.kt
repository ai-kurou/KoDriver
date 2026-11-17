package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface LowVolumeWarningEnabledPreferencesRepository {
    public fun observeLowVolumeWarningEnabled(): Flow<Boolean>

    public suspend fun saveLowVolumeWarningEnabled(enabled: Boolean)
}
