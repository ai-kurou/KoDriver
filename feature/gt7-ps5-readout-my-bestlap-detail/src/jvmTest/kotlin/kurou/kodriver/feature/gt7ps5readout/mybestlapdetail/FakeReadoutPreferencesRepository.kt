package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

class FakeReadoutPreferencesRepository : ReadoutPreferencesRepository {
    private val enabledStates = MutableStateFlow<Map<Pair<String, ReadoutItemKey>, Boolean>>(emptyMap())
    private val orders = MutableStateFlow<Map<String, List<ReadoutItemKey>>>(emptyMap())

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        enabledStates.map { states ->
            states.filterKeys { it.first == simulator }.mapKeys { it.key.second }
        }

    override suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean) {
        enabledStates.update { it + ((simulator to key) to enabled) }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> =
        orders.map { it[simulator] ?: emptyList() }

    override suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>) {
        orders.update { it + (simulator to order) }
    }
}
