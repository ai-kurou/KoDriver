package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository

class ObserveLmuWindowsVehicleDamageUseCase(private val repository: LmuWindowsVehicleDamageRepository) {
    operator fun invoke(): Flow<LmuWindowsVehicleDamageData> = repository.vehicleDamageStream()
}
