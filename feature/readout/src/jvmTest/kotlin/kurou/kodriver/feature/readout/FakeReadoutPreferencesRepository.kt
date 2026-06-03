package kurou.kodriver.feature.readout

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class FakeReadoutPreferencesRepository : ReadoutPreferencesRepository {
    private val _states = MutableStateFlow<Map<String, Map<String, Boolean>>>(emptyMap())

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
        _states.map { it[simulator] ?: emptyMap() }

    override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) {
        _states.update { all ->
            val current = all[simulator] ?: emptyMap()
            all + (simulator to (current + (label to enabled)))
        }
    }
}
