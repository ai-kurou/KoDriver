package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository

internal class FakeVehicleDamagePreferencesRepository(
    initialStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
) : VehicleDamagePreferencesRepository {
    private val states = MutableStateFlow(initialStates)

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}
