package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository

internal class FakeVehicleDamagePreferencesRepository(
    initialStates: Map<String, Boolean> = emptyMap(),
) : VehicleDamagePreferencesRepository {
    private val states = MutableStateFlow(initialStates)

    override fun observeEnabledStates(): Flow<Map<String, Boolean>> = states

    override suspend fun saveEnabledState(key: String, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}
