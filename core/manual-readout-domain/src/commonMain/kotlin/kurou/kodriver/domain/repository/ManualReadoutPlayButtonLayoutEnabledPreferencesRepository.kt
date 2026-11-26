package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface ManualReadoutPlayButtonLayoutEnabledPreferencesRepository {
    public fun observeManualReadoutPlayButtonLayoutEnabled(): Flow<Boolean>

    public suspend fun saveManualReadoutPlayButtonLayoutEnabled(enabled: Boolean)

    public suspend fun resetToDefault()
}
