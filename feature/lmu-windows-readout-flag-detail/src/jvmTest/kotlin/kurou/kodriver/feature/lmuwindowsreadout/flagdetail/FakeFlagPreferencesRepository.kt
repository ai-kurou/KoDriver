package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.FlagPreferencesRepository

class FakeFlagPreferencesRepository(
    initialStates: Map<String, Boolean> = emptyMap(),
) : FlagPreferencesRepository {
    private val states = MutableStateFlow(initialStates)

    override fun observeFlagEnabledStates(): Flow<Map<String, Boolean>> = states

    override suspend fun saveFlagEnabledState(key: String, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}
