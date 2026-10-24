package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface ManualReadoutPlayButtonLayoutEnabledPreferencesRepository {
    fun observeManualReadoutPlayButtonLayoutEnabled(): Flow<Boolean>

    suspend fun saveManualReadoutPlayButtonLayoutEnabled(enabled: Boolean)
}
