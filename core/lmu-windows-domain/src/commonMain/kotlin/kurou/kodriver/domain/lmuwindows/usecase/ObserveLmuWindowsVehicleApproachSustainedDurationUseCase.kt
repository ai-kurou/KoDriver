package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

public class ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(
    private val repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository,
) {
    public operator fun invoke(): Flow<Int> = repository.observeSustainedApproachDurationSeconds()
}
