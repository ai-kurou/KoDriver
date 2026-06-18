package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

internal class FakeVehicleApproachPreferencesRepository(
    initialSkipFirstLap: Boolean = true,
    initialStartReadoutEnabled: Boolean = true,
    initialStartReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
) : VehicleApproachPreferencesRepository {
    private data class State(
        val skipFirstLap: Boolean,
        val startReadoutEnabled: Boolean,
        val startReadoutType: VehicleApproachStartReadoutType,
    )

    private val state = MutableStateFlow(
        State(
            skipFirstLap = initialSkipFirstLap,
            startReadoutEnabled = initialStartReadoutEnabled,
            startReadoutType = initialStartReadoutType,
        ),
    )

    override fun observeSkipFirstLap(): Flow<Boolean> = state.map { it.skipFirstLap }

    override suspend fun saveSkipFirstLap(skip: Boolean) {
        state.update { it.copy(skipFirstLap = skip) }
    }

    override fun observeStartReadoutEnabled(): Flow<Boolean> = state.map { it.startReadoutEnabled }

    override suspend fun saveStartReadoutEnabled(enabled: Boolean) {
        state.update { it.copy(startReadoutEnabled = enabled) }
    }

    override fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> = state.map { it.startReadoutType }

    override suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) {
        state.update { it.copy(startReadoutType = type) }
    }
}
