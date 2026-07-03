package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

class ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeSkipFirstLap()
}
