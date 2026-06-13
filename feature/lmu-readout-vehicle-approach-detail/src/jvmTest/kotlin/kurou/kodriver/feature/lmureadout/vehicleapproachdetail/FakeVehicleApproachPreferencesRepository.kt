package kurou.kodriver.feature.lmureadout.vehicleapproachdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

internal class FakeVehicleApproachPreferencesRepository(
    initialSkipFirstLap: Boolean = true,
) : VehicleApproachPreferencesRepository {
    private val state = MutableStateFlow(initialSkipFirstLap)

    override fun observeSkipFirstLap(): Flow<Boolean> = state

    override suspend fun saveSkipFirstLap(skip: Boolean) {
        state.update { skip }
    }
}
