package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository

class ObserveLmuWindowsVehicleClassUseCase(
    private val repository: LmuWindowsVehicleClassRepository,
) {
    operator fun invoke(): Flow<LmuWindowsVehicleClassData> = repository.vehicleClassStream()
}
