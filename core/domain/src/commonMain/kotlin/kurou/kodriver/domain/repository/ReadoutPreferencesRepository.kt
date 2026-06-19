package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface ReadoutPreferencesRepository {
    fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>>
    suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean)
    fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>>
    suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>)
}
