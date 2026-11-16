package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachRepository

public class ObserveAceWindowsVehicleApproachUseCase(
    private val repository: AceWindowsVehicleApproachRepository,
) {
    public operator fun invoke(): Flow<AceWindowsVehicleApproachData> = repository.vehicleApproachStream()
}
