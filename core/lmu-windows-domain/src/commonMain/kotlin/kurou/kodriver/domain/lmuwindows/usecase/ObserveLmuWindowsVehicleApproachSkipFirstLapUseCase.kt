package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository

public class ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.observeSkipFirstLap()
}
