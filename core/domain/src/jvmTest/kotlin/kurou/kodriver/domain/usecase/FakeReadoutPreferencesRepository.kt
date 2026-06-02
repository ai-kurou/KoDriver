package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class FakeReadoutPreferencesRepository : ReadoutPreferencesRepository {
    private val states = MutableStateFlow<Map<String, Map<String, Boolean>>>(emptyMap())

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
        states.map { it[simulator] ?: emptyMap() }

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) {
        states.value = states.value.toMutableMap().apply {
            this[simulator] = (this[simulator] ?: emptyMap()) + (label to enabled)
        }
    }
}
