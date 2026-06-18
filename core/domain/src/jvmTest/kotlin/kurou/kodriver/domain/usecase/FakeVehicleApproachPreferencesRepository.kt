package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

internal class FakeVehicleApproachPreferencesRepository(
    initialSkipFirstLap: Boolean = true,
    initialStartReadoutEnabled: Boolean = true,
) : VehicleApproachPreferencesRepository {
    private data class State(
        val skipFirstLap: Boolean,
        val startReadoutEnabled: Boolean,
    )

    private val state = MutableStateFlow(
        State(
            skipFirstLap = initialSkipFirstLap,
            startReadoutEnabled = initialStartReadoutEnabled,
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
}
