package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamageRepository

class ObserveLmuWindowsVehicleDamageUseCase(
    private val repository: LmuWindowsVehicleDamageRepository,
) {
    operator fun invoke(): Flow<LmuWindowsVehicleDamageData> = repository.vehicleDamageStream()
}
