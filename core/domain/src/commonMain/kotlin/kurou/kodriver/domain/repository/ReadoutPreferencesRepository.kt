package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface ReadoutPreferencesRepository {
    fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>>
    suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean)
    fun observeReadoutOrder(simulator: String): Flow<List<String>>
    suspend fun saveReadoutOrder(simulator: String, order: List<String>)
}
