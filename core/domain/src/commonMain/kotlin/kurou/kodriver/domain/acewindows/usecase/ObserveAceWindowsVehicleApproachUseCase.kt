package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachRepository

class ObserveAceWindowsVehicleApproachUseCase(
    private val repository: AceWindowsVehicleApproachRepository,
) {
    operator fun invoke(): Flow<AceWindowsVehicleApproachData> = repository.vehicleApproachStream()
}
