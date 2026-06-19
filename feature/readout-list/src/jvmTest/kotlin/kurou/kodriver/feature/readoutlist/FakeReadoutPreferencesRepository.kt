package kurou.kodriver.feature.readoutlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class FakeReadoutPreferencesRepository : ReadoutPreferencesRepository {
    private data class SimulatorState(
        val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
        val itemOrder: List<ReadoutItemKey> = emptyList(),
    )

    private val _states = MutableStateFlow<Map<String, SimulatorState>>(emptyMap())

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        _states.map { it[simulator]?.enabledStates ?: emptyMap() }

    override suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean) {
        _states.update { all ->
            val current = all[simulator] ?: SimulatorState()
            all + (simulator to current.copy(enabledStates = current.enabledStates + (key to enabled)))
        }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> =
        _states.map { it[simulator]?.itemOrder ?: emptyList() }

    override suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>) {
        _states.update { all ->
            val current = all[simulator] ?: SimulatorState()
            all + (simulator to current.copy(itemOrder = order))
        }
    }
}
