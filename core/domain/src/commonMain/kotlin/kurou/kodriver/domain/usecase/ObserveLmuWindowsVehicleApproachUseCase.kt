package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository

class ObserveLmuWindowsVehicleApproachUseCase(
    private val repository: LmuWindowsVehicleApproachRepository,
) {
    operator fun invoke(): Flow<LmuWindowsVehicleApproachData> = repository.vehicleApproachStream()
}
