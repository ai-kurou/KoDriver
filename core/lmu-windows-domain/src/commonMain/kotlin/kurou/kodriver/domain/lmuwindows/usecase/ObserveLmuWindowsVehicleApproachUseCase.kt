package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository

public class ObserveLmuWindowsVehicleApproachUseCase(
    private val repository: LmuWindowsVehicleApproachRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsVehicleApproachData> = repository.vehicleApproachStream()
}
