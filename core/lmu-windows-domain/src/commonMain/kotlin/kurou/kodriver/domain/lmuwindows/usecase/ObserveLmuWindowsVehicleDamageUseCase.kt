package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamageRepository

public class ObserveLmuWindowsVehicleDamageUseCase(
    private val repository: LmuWindowsVehicleDamageRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsVehicleDamageData> = repository.vehicleDamageStream()
}
