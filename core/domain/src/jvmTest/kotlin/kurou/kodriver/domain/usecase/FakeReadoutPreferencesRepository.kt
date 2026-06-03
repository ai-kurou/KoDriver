package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class FakeReadoutPreferencesRepository : ReadoutPreferencesRepository {
    private data class SimulatorState(
        val enabledStates: Map<String, Boolean> = emptyMap(),
        val itemOrder: List<String> = emptyList(),
    )

    private val states = MutableStateFlow<Map<String, SimulatorState>>(emptyMap())

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
        states.map { it[simulator]?.enabledStates ?: emptyMap() }

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) {
        states.update { map ->
            val current = map[simulator] ?: SimulatorState()
            map + (simulator to current.copy(enabledStates = current.enabledStates + (label to enabled)))
        }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<String>> =
        states.map { it[simulator]?.itemOrder ?: emptyList() }

    override suspend fun saveReadoutOrder(simulator: String, order: List<String>) {
        states.update { map ->
            val current = map[simulator] ?: SimulatorState()
            map + (simulator to current.copy(itemOrder = order))
        }
    }
}
