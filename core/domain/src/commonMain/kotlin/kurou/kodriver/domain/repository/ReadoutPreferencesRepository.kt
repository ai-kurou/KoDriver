package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface ReadoutPreferencesRepository {
    fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>>
    suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean)
}
