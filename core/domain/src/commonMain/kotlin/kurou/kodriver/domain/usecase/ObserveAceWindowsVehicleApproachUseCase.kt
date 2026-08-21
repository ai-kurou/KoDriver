package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository

class ObserveAceWindowsVehicleApproachUseCase(
    private val repository: AceWindowsVehicleApproachRepository,
) {
    operator fun invoke(): Flow<AceWindowsVehicleApproachData> = repository.vehicleApproachStream()
}
