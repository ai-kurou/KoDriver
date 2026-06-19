package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.FlagPreferencesRepository

class FakeFlagPreferencesRepository(
    initialStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
) : FlagPreferencesRepository {
    private val states = MutableStateFlow(initialStates)

    override fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states

    override suspend fun saveFlagEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}
