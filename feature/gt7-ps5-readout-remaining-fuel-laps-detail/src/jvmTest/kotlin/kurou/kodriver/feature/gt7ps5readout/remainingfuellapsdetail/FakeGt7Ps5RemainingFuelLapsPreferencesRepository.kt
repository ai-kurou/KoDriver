package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

class FakeGt7Ps5RemainingFuelLapsPreferencesRepository : Gt7Ps5RemainingFuelLapsPreferencesRepository {
    private val remainingFuelLaps = MutableStateFlow(3)

    override fun observeRemainingFuelLaps(): Flow<Int> = remainingFuelLaps

    override suspend fun saveRemainingFuelLaps(laps: Int) {
        remainingFuelLaps.update { laps }
    }
}
